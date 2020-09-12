(ns bucks.import.components.importer
  (:require [reagent.core :as r]
            [cljs-bean.core :refer [->js ->clj]]
            ["papaparse" :refer (parse)]
            ["moment" :as moment]
            [clojure.string :as string]))


(defonce *result (r/atom {}))
(defonce *header-index (r/atom 0))
(defonce *ignored (r/atom #{}))
(defonce *header-types (r/atom {}))
(defonce *date-format (r/atom "YYYY/MM/DD"))

(def header-types #{:date :description :amount :amount-in :amount-out :balance :note})


(defn- reset []
  (reset! *ignored #{}))


(defn- restart []
  (reset)
  (reset! *result {}))


(defn- parse-number [s]
  (try
    (let [n (js/parseFloat
             (string/replace
              (string/replace s #"," "")
              #"\s" ""))]
      (when (js/isNaN n)
        (throw (js/Error. (str "Cannot convert to number: " s))))
      n)
    (catch :default e
      (throw (js/Error. (str "Cannot convert to number: " s))))))

(defn- parse-date [s]
  (let [d (moment s @*date-format true)]
    (when-not (.isValid d)
      (throw (js/Error. (str "Cannot Format Date " s " using " @*date-format))))
    (.format d "YYYY-MM-DD")))

(defn- parse-final-row [o]
  (->> o
       (map (fn [[h v]]
              (let [t (get @*header-types h)]
                (when t
                  (let [v (case t
                            :amount (parse-number v)
                            :amount-in (parse-number v)
                            :amount-out (parse-number v)
                            :balance (parse-number v)
                            :date (parse-date v)
                            v)]
                    [t v])))))
       (remove nil?)
       (into {})))


(defn- parse-final [header rows]
  (try
    (let [rows (->> rows
                    (map-indexed (fn [i row]
                                   (let [o (zipmap header row)]
                                     (assoc (parse-final-row o) :import-index i))))
                    doall)
          desc? (> (:date (first rows)) (:date (last rows)))
          total (dec (count rows))
          index (if desc? #(- total %) identity)]
      (map #(update % :import-index index) rows))
    (catch js/Error e
      (js/alert e)
      nil)))


(defn- event->file [e]
  (aget (.. e -target -files) 0))


(defn- parse' [file f]
  (parse file
         (->js {:complete f
                :skipEmptyLines true
                :transform string/trim})))


(defn- process-result [result]
  (let [r (->clj result)]
    (assoc r :cols (->> (:data r)
                        (map count)
                        (apply max)))))


(defn- handle-file [e]
  (parse' (event->file e)
          #(reset! *result (process-result %))))


(defn- upload-button []
  [:form.file.is-primary.is-small {:id "upload-form"}
   [:label.file-label
    [:input.file-input
     {:type "file"
      :name "upload"
      :on-change handle-file}]
    [:span.file-cta
     #_[:span.file-icon
      [:i.fas.fa-upload]]
     [:span.file-label "Choose a file"]]]])


(defn- result-table []
  (let [{:keys [errors meta data cols]} @*result
        ignored @*ignored
        header-index @*header-index]
    (when-not (empty? data)
      [:div.table-container.is-size-7
       [:table.table.is-narrow.is-hoverable.is-fullwidth
        [:thead
         [:tr
          [:th "ignore?"]
          [:th "header?"]
          [:th {:col-span cols}]]]
        [:tbody
         (->> data
              (map-indexed
               (fn [i r]
                 (let [n (count r)
                       header? (= header-index i)]
                   (when-not (contains? ignored i)
                     [:tr {:key i :class (when header? "is-selected")}
                      [:td
                       [:label.checkbox
                        [:input {:type "checkbox"
                                 :on-click #(swap! *ignored conj i)}]]]
                      [:td
                       [:label.checkbox
                        [:input {:type "checkbox"
                                 :on-click #(reset! *header-index i)
                                 :checked header?}]]]
                      (->> r
                           (map-indexed
                            (fn [j d]
                              [:td {:key j} d])))
                      (->> (range (- cols n))
                           (map
                            (fn [k]
                              [:td {:key k}])))])))))]]])))


(defn- match [done]
  (let [{:keys [data]} @*result
        ignored @*ignored
        header-index @*header-index
        selected-types @*header-types
        has-header? (number? header-index)
        header-not-ignored? (not (contains? ignored header-index))
        date-format @*date-format]
    (if-not (and has-header? header-not-ignored?)
      [:div.has-text-info "Please choose a header"]
      (let [header (nth data header-index)
            header-length (count header)
            rows (->> data
                      (map-indexed
                       (fn [i r]
                         [(and (not= i header-index) (not (contains? ignored i))) r]))
                      (filter first)
                      (map last))
            all-rows-correct-length? (every? (fn [r] (= header-length (count r))) rows)]
        (if-not all-rows-correct-length?
          [:div.has-text-info "Please ignore invalid rows"]
          (let [chosen-headers (frequencies (vals selected-types))
                once? (fn [t] (= 1 (get chosen-headers t)))
                once-optional? (fn [t]
                                 (<= (get chosen-headers t) 1))
                continue? (and (once? :date) (once? :description)
                               (or (once? :amount) (and (once? :amount-in) (once? :amount-out)))
                               (once-optional? :balance) (once-optional? :note))]
            (if (empty? header)
              [:div "Please upload a .csv file"]
              [:div.columns
               [:div.column
                [:h2.heading "Please match your columns"]
                [:ul.content.is-size-7
                 [:li "Import assumes entries are ordered by date (either ascending or decending)"]
                 [:li "Numbers should be formatted as " [:i "236543.45"]]
                 [:li [:strong "Required"]
                  [:ul.mt-0
                   [:li [:strong "Date: "] "Transaction Date. Remember to choose the correct date format."]
                   [:li [:strong "Description: "] "Unique test describing the transaction. (not editable after import)"]
                   [:li [:strong "Amount: "] "either"
                    [:ul
                     [:li [:strong "Amount: "] "A positive or negative value"]
                     [:li [:strong "Amount in "] "and " [:strong "Amount out. "]
                      "Both should be zero or more."]
                     ]]]]
                 [:li [:strong "Optional"]
                  [:ul.mt-0
                   [:li [:strong "Balance: "]
                    "An amount that indicates the account balance after transaction."
                    [:ul
                     [:li "When not included, the balance will be auto calculated based on previous transactions"]
                     [:li "When included, it resets the account balance at that date."]
                     [:li "For pure balance reconciliation, leave amount or amount-in/amount-out as zero."]]]
                   [:li [:strong "Note: "] "Extra text that can be editited after import."]]]
                 ]]
               [:div.column
                [:div
                 (->> header
                      (map-indexed
                       (fn [i h]
                         [:div.field.is-horizontal {:key i}
                          [:div.field-label.is-small [:label.label h]]
                          [:div.field.field-body
                           [:div.field.is-narrow
                            [:div.control
                             [:div.select.is-fullwidth.is-small
                              [:select {:value (get selected-types h "Please Select")
                                        :on-change #(swap! *header-types
                                                           assoc h (keyword (.. % -target -value)))}
                               (when-not (contains? selected-types h)
                                 [:option "Please Select"])
                               (->> header-types
                                    (map-indexed
                                     (fn [i t]
                                       [:option
                                        {:key i}
                                        (name t)])))
                               ]]]]]])))
                 [:div.field.is-horizontal
                  [:div.field-label.is-small [:label.label "Date Format"]]
                  [:div.field-body
                   [:div.field.is-narrow
                    [:div.control
                     [:input.input.is-fullwidth.is-small
                      {:value date-format
                       :on-change #(reset! *date-format (.. % -target -value))}]]
                    [:p.help.has-text-right [:a {:href "https://momentjs.com/docs/#/parsing/string-format/" :target "_blank"} "examples"]]]]]]
                (when continue?
                  [:div.field.is-horizontal
                   [:div.field-label.is-small]
                   [:div.field-body
                    [:div.field.is-narrow
                     [:div.control
                      [:button.button.is-primary.is-small
                       {:on-click (fn []
                                    (when-let [final (parse-final header rows)]
                                      (done {:entries final
                                             :date-format @*date-format
                                             :header-types @*header-types
                                             :header-index @*header-index})))}
                       "Upload"]]]]])]])))))))


(defn component [opts done]
  (r/create-class
   {:name "importer"
    :constructor (fn []
                   (restart)
                   (let [{:keys [date-format header-types header-index]} opts]
                     (if-not (string/blank? date-format)
                       (reset! *date-format date-format)
                       (reset! *date-format "YYYY/MM/DD"))
                     (if (map? header-types)
                       (reset! *header-types header-types)
                       (reset! *header-types {}))
                     (if (integer? header-index)
                       (reset! *header-index header-index)
                       (reset! *header-index 0))))
    :reagent-render
    (fn [_ done]
      [:div
       [:div.buttons
        [upload-button]
        [:button.button.mt-2.ml-2.is-small {:on-click restart} "Restart"]
        [:button.button.mt-2.is-small {:on-click reset} "Reset"]]
       [match done]
       [result-table]])}))
