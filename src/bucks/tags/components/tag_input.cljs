(ns bucks.tags.components.tag-input
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [clojure.string :as string]
            [cljs-bean.core :refer [->js ->clj]]
            ["@yaireo/tagify/dist/react.tagify" :as Tags]
            [bucks.tags.core :as tags.core]
            [bucks.tags.state :as tags.state]))


(defn- tags->vals [available-tags ts]
  (let [lookup (->> available-tags
                    (map (juxt :id :label))
                    (into {}))]
    (->> ts
         (map (fn [t] (get lookup t)))
         (remove nil?)
         ->js
         js/JSON.stringify)))


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
  (let [v (.. e -target -value)]
    (if (string/blank? v)
      []
      (->> v
           js/JSON.parse
           ->clj
           (map :value)))))


(defn- extract-changes [available-tags labels]
  (let [existing (->> available-tags
                      (map (juxt :label identity))
                      (into {}))
        items (->> labels
                   (map (fn [l]
                          (get existing l
                               (assoc (tags.core/new-tag l) :new? true))))
                   (group-by :new?))
        old (get items nil)
        new (->> (get items true)
                 (map #(dissoc % :new?)))]
    {:new new
     :changes (->> (concat old new)
                   (map :id))}))


(def ^:private tags-r (r/adapt-react-class Tags))


(defn tags [t & {:keys [placeholder on-change created]
                 :or {placeholder "add tags"
                      on-change #(prn "Changed: " %)}}]
  (r/with-let [available-tags @(rf/subscribe [::tags.state/available-tags])
               value (tags->vals available-tags t)]
    [tags-r {:settings {:placeholder placeholder
                        :whitelist (map :label available-tags)
                        :transformTag #(apply-color available-tags %)
                        :dropdown {:enabled 0}}
             :value value
             :on-change (fn [e]
                          (let [labels (extract-vals e)
                                {:keys [new changes]} (extract-changes available-tags labels)]
                            (doseq [t new]
                              (tags.state/add-tag t))
                            (on-change changes)))}]))
