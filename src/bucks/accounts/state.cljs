(ns bucks.accounts.state
  (:require [re-frame.core :as rf]
            [bucks.accounts.core :as accounts]))


(defn init-state [s]
  (assoc s ::accounts {}))


(rf/reg-sub
 ::accounts
 (fn [db _]
   (vals (::accounts db))))


(rf/reg-event-db
 ::add-account
 (fn [db [_ id]]
   (assoc-in db [::accounts id] (accounts/new-account id))))


(defn add-account []
  (rf/dispatch [::add-account (str (random-uuid))]))


(rf/reg-event-db
 ::remove-account
 (fn [db [_ id]]
   (update db ::accounts dissoc id)))


(defn remove-account [id]
  (when (js/confirm "You cannot undo account removal! Proceed?")
    (rf/dispatch [::remove-account id])))


(rf/reg-event-db
 ::update-account
 (fn [db [_ id path value]]
   (assoc-in db (concat [::accounts id] path) value)))


(defn update-account [id path value]
  (rf/dispatch [::update-account id
                (if (keyword? path) [path] path)
                value]))


(rf/reg-event-db
 ::select-account
 (fn [db [_ id]]
   (assoc db ::selected-account id)))


(defn select-account [id]
  (rf/dispatch [::select-account id]))


(rf/reg-sub
 ::selected-account
 (fn [db _]
   (::selected-account db)))


(rf/reg-sub
 ::account-data
 (fn [db [_ id]]
   (->
    (get-in db [::accounts id])
    (dissoc :entries))))


(rf/reg-sub
 ::account-entries
 (fn [db [_ id]]
   (->
    (get-in db [::accounts id])
    (get :entries)
    vals
    accounts/sort-entries
    reverse)))


(rf/reg-sub
 ::account-name
 (fn [db [_ id]]
   (let [n (get-in db [::accounts id :name])]
     (if (empty? n) "[account name missing]" n))))


(rf/reg-event-db
 ::add-entries
 (fn [db [_ account-id e]]
   (let [{:keys [entries current-balance current-balance-base] :as d}
         (->> (get-in db [::accounts account-id :entries])
              vals
              (into e)
              accounts/re-balance)
         entries-m (->> entries
                        (map (juxt :id identity))
                        (into {}))]
     (-> db
         (assoc-in [::accounts account-id :entries] entries-m)
         (assoc-in [::accounts account-id :current-balance] current-balance)
         (assoc-in [::accounts account-id :current-balance-base] current-balance-base)))))


(defn add-entries [account-id e]
  (rf/dispatch [::add-entries account-id e]))


(rf/reg-event-db
 ::update-entry
 (fn [db [_ account-id entry-id k v]]
   (assoc-in db [::accounts account-id :entries entry-id k] v)))


(defn- update-entry [account-id entry-id k v]
  (rf/dispatch [::update-entry account-id entry-id k v]))


(defn update-entry-note [account-id entry-id v]
  (update-entry account-id entry-id :note v))


(defn update-entry-tags [account-id entry-id v]
  (update-entry account-id entry-id :tags (set v)))
