(ns bucks.shared
  (:require ["@yaireo/tagify/dist/react.tagify" :as Tags]
            [clojure.string :as string]
            [reagent.core :as r]
            [cljs-bean.core :refer [->clj]]
            [bucks.pages.core :as pages]))


(defn heading [& s]
  [:div
   [:h1.heading
    (map-indexed (fn [i v] [:<> {:key i} v]) s)]
   [:hr]])


(defn back
  ([t] (back t pages/go-back))
  ([t f]
   [:small.is-pulled-right [:a {:on-click f} t]]))


(defn table [& children]
  [:div.table-container.is-size-7
   [:table.table.is-striped
    (map-indexed
     (fn [i c] [:<> {:key i} c])
     children)]])


(defn table-cell-input [value & {:keys [placeholder on-change]
                                 :or {placeholder "I need a name"
                                      on-change prn}}]
  (let [change (fn [e] (on-change (.. e -target -value)))]
    [:input.input.is-small
     {:value value
      :placeholder placeholder
      :on-change change}]))



(def ^:private tags-r (r/adapt-react-class Tags))

(defn select [options value & {:keys [placeholder on-change]
                               :or {placeholder "Please select"
                                    on-change prn}}]
  (let [o-map (->> options
                   (map (juxt :label :value))
                   (into {}))]
    [tags-r {:settings {:placeholder placeholder
                        :mode "select"
                        :whitelist (->> options
                                        (map :label))}
             :value value
             :on-change (fn [e]
                          (let [v (.. e -target -value)]
                            (when-not (string/blank? v)
                              (let [v' (->> (js/JSON.parse v)
                                            ->clj
                                            first
                                            :value
                                            (get o-map))]
                                (when-not (nil? v')
                                  (on-change v'))))))}]))

(defn- level-item [heading title & {:keys [title-class]}]
  [:div.level-item.has-text-centered
   [:div
    [:p.heading heading]
    [:p.title
     (if title-class {:class title-class} {})
     title]]])
