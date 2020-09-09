(ns bucks.components.tags
  (:require [reagent.core :as r]
            [clojure.string :as string]
            [cljs-bean.core :refer [->js ->clj]]
            ["@yaireo/tagify/dist/react.tagify" :as Tags]))


(defn- tags->vals [ts]
  (->> ts
       (map (fn [t] {:value (:label t)
                     :editable false}))
       ->js
       js/JSON.stringify))


(defn- apply-color [available-tags tag]
  (let [colors (->> available-tags
                    (map (juxt :label :color))
                    (into {}))
        color (get colors (. tag -value))]
    (when color
      (set! (. tag -style) (str "--tag-bg:" color
                                ";--tag-text-color:white"
                                ";--tag-remove-btn-color:white")))))

(defn- extract-vals [e]
  (->> (.. e -target -value)
       js/JSON.parse
       ->clj
       (map :value)))


(defn- extract-changes [available-tags labels]
  (let [existing (->> available-tags
                      (map (juxt :label identity))
                      (into {}))
        items (->> labels
                   (map (fn [l]
                          (get existing l
                               {:id (str (random-uuid))
                                :label l
                                :color "#e5e5e5"
                                :new? true})))
                   (group-by :new?))
        old (get items nil)
        new (->> (get items true)
                 (map #(dissoc % :new?)))]
    {:new new
     :changes (concat old new)}))


(def ^:private tags-r (r/adapt-react-class Tags))

(defn tags [t available-tags & {:keys [placeholder on-change created]
                                      :or {placeholder "add tags"
                                           created #(prn "Created: " %)
                                           on-change #(prn "Changed: " %)}}]
  [tags-r {:settings {:placeholder placeholder
                      :whitelist (map :label available-tags)
                      :transformTag #(apply-color available-tags %)
                      :dropdown {:enabled 0}}
           :value (tags->vals t)
           :on-change (fn [e]
                        (let [labels (extract-vals e)
                              {:keys [new changes]} (extract-changes available-tags labels)]
                          (when-not (empty? new)
                            (created new))
                          (on-change changes)))}])
