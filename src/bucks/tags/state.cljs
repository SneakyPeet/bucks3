(ns bucks.tags.state
  (:require [re-frame.core :as rf]
            [bucks.tags.core :as tags.core]
            [bucks.accounts.state :as accounts.state]))


(defn init-state [db localstore]
  (assoc db
         ::available-tags (:available-tags localstore {})
         ::tag-groups (:tag-groups localstore {})))


(rf/reg-sub
 ::available-tags
 (fn [db _]
   (->> (::available-tags db {})
        (vals))))


(rf/reg-sub
 ::available-tags-map
 (fn [db _]
   (->> (::available-tags db {}))))


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
 ::remove-tag
 [(rf/inject-cofx :localstore)]
 (fn [{:keys [db localstore]} [_ tag-id]]
   {:db (update db ::available-tags dissoc tag-id)
    :localstore (update localstore :available-tags dissoc tag-id)}))


(rf/reg-event-fx
 ::update-tag
 [(rf/inject-cofx :localstore)]
 (fn [{:keys [db localstore]} [_ id k v]]
   {:db (assoc-in db [::available-tags id k] v)
    :localstore (assoc-in localstore [:available-tags id k] v)}))


(defn add-tag
  ([] (add-tag (tags.core/new-tag "NEW")))
  ([tag] (rf/dispatch [::add-tag tag])))


(defn remove-tag
  [tag-id] (rf/dispatch [::remove-tag tag-id]))


(defn- update-tag [id k v]
  (rf/dispatch [::update-tag id k v]))


(defn update-label [id label]
  (update-tag id :label label))


(defn update-color [id color]
  (update-tag id :color color))


(rf/reg-event-db
 ::select-tag
 (fn [db [_ id]]
   (assoc db ::selected-tag id)))


(rf/reg-sub
 ::selected-tag
 (fn [db _]
   (get-in db [::available-tags (::selected-tag db)])))


(defn select-tag [id]
  (rf/dispatch [::select-tag id]))


(rf/reg-sub
 ::tag-entries
 (fn [db _]
   (let [tag-id (::selected-tag db)
         accounts (::accounts.state/accounts db)]
     (->> accounts
          vals
          (map (fn [{:keys [entries name currency id] :as a}]
                 (->> entries
                      vals
                      (filter #(contains? (:tags %) tag-id))
                      (map #(assoc %
                                   :account-name name
                                   :account-id id
                                   :currency currency)))))
          (reduce into)
          (sort-by :date)
          reverse))))


(rf/reg-sub
 ::tag-groups-map
 (fn [db _]
   (->> (::tag-groups db))))


(rf/reg-sub
 ::tag-groups
 :<- [::tag-groups-map]
 (fn [g _]
   (vals g)))


(rf/reg-sub
 ::tag-group-color-names
 :<- [::tag-groups]
 (fn [g _]
   (->> g
        (map (juxt :color :name))
        (into {}))))


(rf/reg-event-fx
 ::update-tag-group
 [(rf/inject-cofx :localstore)]
 (fn [{:keys [db localstore]} [_ group]]
   {:db (assoc-in db [::tag-groups (:id group)] group)
    :localstore (assoc-in localstore [:tag-groups (:id group)] group)}))


(defn add-tag-group []
  (rf/dispatch [::update-tag-group (tags.core/new-tag-group "black" "Default")]))


(defn edit-tag-group-name [tag new-name]
  (prn tag new-name)
  (rf/dispatch [::update-tag-group (assoc tag :name new-name)]))


(defn edit-tag-group-color [tag new-color]
  (rf/dispatch [::update-tag-group (assoc tag :color new-color)]))
