(ns bucks.tags.state
  (:require [re-frame.core :as rf]
            [bucks.tags.core :as tags.core]))


(defn init-state [db localstore]
  (assoc db ::available-tags (:available-tags localstore {})))

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


(rf/reg-event-fx
 ::add-tag
 [(rf/inject-cofx :localstore)]
 (fn [{:keys [db localstore]} [_ tag]]
   {:db (assoc-in db [::available-tags (:id tag)] tag)
    :localstore (assoc-in localstore [:available-tags (:id tag)] tag)}))


(rf/reg-event-fx
 ::update-tag
 [(rf/inject-cofx :localstore)]
 (fn [{:keys [db localstore]} [_ id k v]]
   {:db (assoc-in db [::available-tags id k] v)
    :localstore (assoc-in localstore [:available-tags id k] v)}))


(defn add-tag
  ([] (add-tag (tags.core/new-tag "NEW")))
  ([tag] (rf/dispatch [::add-tag tag])))


(defn- update-tag [id k v]
  (rf/dispatch [::update-tag id k v]))


(defn update-label [id label]
  (update-tag id :label label))


(defn update-color [id color]
  (update-tag id :color color))
