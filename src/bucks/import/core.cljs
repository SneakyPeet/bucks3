(ns bucks.import.core
  (:require [reagent.core :as r]
            [cljs-bean.core :refer [->js ->clj]]
            ["papaparse" :refer (parse)]
            ["moment" :as moment]
            [clojure.string :as string]))


(defonce *result (r/atom {}))
(defonce *header-index (r/atom nil))
(defonce *ignored (r/atom #{}))
(defonce *header-types (r/atom {}))
(defonce *date-format (r/atom "YYYY/MM/DD"))

(def header-types [:date :description :amount :amount-in :amount-out :text :number :date-other])


(defn- reset []
  (reset! *header-index nil)
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
                  (let [k (case t
                            :date :date
                            :amount :amount
                            :amount-in :amount-in
                            :amount-out :amount-out
                            :description :description
                            h)
                        v (case t
                            :amount (parse-number v)
                            :amount-in (parse-number v)
                            :amount-out (parse-number v)
                            :number (parse-number v)
                            :date (parse-date v)
                            :date-other (parse-date v)
                            v)]
                    [k v])))))
       (remove nil?)
       (into {})))


(defn- parse-final [header rows]
  (try
    (->> rows
         (map (fn [row]
                (let [o (zipmap header row)]
                  (parse-final-row o))))
         doall)
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


(defn- accept-button [done]
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
                continue? (and (once? :date) (once? :description)
                               (or (once? :amount) (and (once? :amount-in) (once? :amount-out))))]
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
                [:p.help.has-text-right [:a {:href "https://momentjs.com/docs/#/parsing/string-format/" :target "_blank"} "examples"]]]]]
             (if-not continue?
               [:div.has-text-info "Please choose at least a Date, Description and Amount or Amount-in and Amount-out"]
               [:div.field.is-horizontal
                [:div.field-label.is-small]
                [:div.field-body
                 [:div.field.is-narrow
                  [:div.control
                   [:button.button.is-primary.is-small
                    {:on-click (fn []
                                 (when-let [final (parse-final header rows)]
                                   (done {:data final
                                          :date-format @*date-format
                                          :header-types @*header-types})))}
                    "Upload"]]]]])]))))))


(defn component [opts done]
  (r/create-class
   {:name "importer"
    :constructor (fn []
                   (restart)
                   (let [{:keys [date-format header-types]} opts]
                     (if-not (string/blank? date-format)
                       (reset! *date-format date-format)
                       (reset! *date-format "YYYY/MM/DD"))
                     (if (map? header-types)
                       (reset! *header-types header-types)
                       (reset! *header-types {}))))
    :reagent-render
    (fn [_ done]
      [:div
       [:div.buttons
        [upload-button]
        [:button.button.mt-2.ml-2.is-small {:on-click restart} "Restart"]
        [:button.button.mt-2.is-small {:on-click reset} "Reset"]]
       [accept-button done]
       [result-table]])}))
