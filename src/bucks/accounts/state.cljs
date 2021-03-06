(ns bucks.accounts.state
  (:require [re-frame.core :as rf]
            [bucks.accounts.core :as accounts]))


(defn init-state [s localstore]
  (assoc s ::accounts (->> localstore
                           :accounts
                           (map (fn [[aid v]]
                                  (let [{:keys [entries] :as d} (->> (:entries v)
                                                                     vals
                                                                     (accounts/re-balance))
                                        account (-> (accounts/new-account aid)
                                                    (merge v (dissoc d :entries))
                                                    (assoc :entries (->> entries
                                                                         (map (juxt :id identity))
                                                                         (into {}))))]
                                    [aid account])))
                           (into {}))))


(rf/reg-sub
 ::accounts
 (fn [db _]
   (vals (::accounts db))))


(rf/reg-event-fx
 ::add-account
 [(rf/inject-cofx :localstore)]
 (fn [{:keys [db localstore]} [_ id]]
   {:db (assoc-in db [::accounts id] (accounts/new-account id))
    :localstore (assoc-in localstore [:accounts id] (accounts/new-account id))}))


(defn add-account []
  (rf/dispatch [::add-account (str (random-uuid))]))


(rf/reg-event-fx
 ::remove-account
 [(rf/inject-cofx :localstore)]
 (fn [{:keys [localstore db]} [_ id]]
   {:db (update db ::accounts dissoc id)
    :localstore (update localstore :accounts dissoc id)}))


(defn remove-account [id]
  (when (js/confirm "You cannot undo account removal! Proceed?")
    (rf/dispatch [::remove-account id])))


(rf/reg-event-fx
 ::update-account
 [(rf/inject-cofx :localstore)]
 (fn [{:keys [db localstore]} [_ id path value]]
   {:db (assoc-in db (concat [::accounts id] path) value)
    :localstore (assoc-in localstore (concat [:accounts id] path) value)}))


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
 ::accounts-by-type
 (fn [db [_ t]]
   (->> (get db ::accounts)
        vals
        (filter #(= t (:account-type %))))))


(rf/reg-sub
 ::account-name
 (fn [db [_ id]]
   (let [n (get-in db [::accounts id :name])]
     (if (empty? n) "[account name missing]" n))))


(defn- apply-entries [db localstore account-id entries]
  (let [{:keys [entries] :as d} (accounts/re-balance entries)
        entries-m (->> entries
                       (map (juxt :id identity))
                       (into {}))]
    {:db (-> db
             (update-in [::accounts account-id] merge d)
             (assoc-in [::accounts account-id :entries] entries-m))
     :localstore (assoc-in localstore [:accounts account-id :entries]
                           (accounts/entries->saveable entries-m))}))


(rf/reg-event-fx
 ::add-entries
 [(rf/inject-cofx :localstore)]
 (fn [{:keys [db localstore]} [_ account-id e]]
   (let [entries (->> (get-in db [::accounts account-id :entries])
                      vals
                      (into e))]
     (apply-entries db localstore account-id entries))))


(defn add-entries [account-id e]
  (rf/dispatch [::add-entries account-id e]))


(rf/reg-event-fx
 ::remove-entries
 [(rf/inject-cofx :localstore)]
 (fn [{:keys [db localstore]} [_ account-id remove?]]
   (let [entries (->> (get-in db [::accounts account-id :entries])
                      vals
                      (remove remove?))]
     (apply-entries db localstore account-id entries))))


(defn remove-entries [account-id pred]
  (rf/dispatch [::remove-entries account-id pred]))


(rf/reg-event-fx
 ::update-entry
 [(rf/inject-cofx :localstore)]
 (fn [{:keys [db localstore]} [_ account-id entry-id k v]]
   {:db (assoc-in db [::accounts account-id :entries entry-id k] v)
    :localstore (assoc-in localstore [:accounts account-id :entries entry-id k] v)}))


(defn- update-entry [account-id entry-id k v]
  (rf/dispatch [::update-entry account-id entry-id k v]))


(defn update-entry-type [account-id entry-id v]
  (update-entry account-id entry-id :type v))


(defn update-entry-note [account-id entry-id v]
  (update-entry account-id entry-id :note v))


(defn update-entry-tags [account-id entry-id v]
  (update-entry account-id entry-id :tags (set v)))
