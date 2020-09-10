(ns bucks.tags.state
  (:require [re-frame.core :as rf]
            [bucks.tags.core :as tags.core]))


(rf/reg-sub
 ::available-tags
 (fn [db _]
   (->> (::available-tags db {})
        (vals))))


(rf/reg-sub
 ::tag-id-labels
 (fn [db _]
   (->> (::available-tags db {})
        (vals)
        (map (juxt :id :label))
        (into {}))))


(rf/reg-sub
 ::tag-label-colors
 (fn [db _]
   (->> (::available-tags db {})
        (vals)
        (map (juxt :label :color))
        (into {}))))


(rf/reg-event-db
 ::add-tag
 (fn [db [_ tag]]
   (assoc-in db [::available-tags (:id tag)] tag)))


(rf/reg-event-db
 ::update-tag
 (fn [db [_ id k v]]
   (assoc-in db [::available-tags id k] v)))


(defn add-tag
  ([] (add-tag (tags.core/new-tag "NEW")))
  ([tag] (rf/dispatch [::add-tag tag])))


(defn- update-tag [id k v]
  (rf/dispatch [::update-tag id k v]))


(defn update-label [id label]
  (update-tag id :label label))


(defn update-color [id color]
  (update-tag id :color color))
