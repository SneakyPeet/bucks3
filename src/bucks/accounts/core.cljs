(ns bucks.accounts.core
  (:require [re-frame.core :as rf]
            [bucks.pages.core :as pages]))

(defn init-state [s]
  (assoc s ::accounts {}))


(defn new-account [id]
  {:id id
   :name ""
   :date-format nil
   :header-types {}
   :entries {}})


(rf/reg-sub
 ::accounts
 (fn [db _]
   (vals (::accounts db))))

(rf/reg-event-db
 ::add-account
 (fn [db [_ id]]
   (assoc-in db [::accounts id] (new-account id))))


(rf/reg-event-db
 ::remove-account
 (fn [db [_ id]]
   (update db ::accounts dissoc id)))


(rf/reg-event-db
 ::update-account
 (fn [db [_ id path value]]
   (assoc-in db (concat [::accounts id] path) value)))


(rf/reg-event-db
 ::select-account
 (fn [db [_ id]]
   (assoc db ::selected-account id)))


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
    vals)))


(defn- add-account []
  (rf/dispatch [::add-account (str (random-uuid))]))


(defn- remove-account [id]
  (when (js/confirm "You cannot undo account removal! Proceed?")
    (rf/dispatch [::remove-account id])))


(defn update-account [id path value]
  (prn  id path value)
  (rf/dispatch [::update-account id
                (if (keyword? path) [path] path)
                value]))


(defn- select-account [id]
  (rf/dispatch [::select-account id]))


(defn component []
  (let [accounts @(rf/subscribe [::accounts])]
    [:div
     [:table.table.is-small
      (when-not (empty? accounts)
        [:thead
         [:tr [:td "Accounts"] [:td] [:td]]])
      [:tbody
       (->> accounts
            (map-indexed
             (fn [i account]
               [:tr {:key i}
                [:td [:input.input.is-small {:value (:name account)
                                             :placeholder "I need a name"
                                             :on-change #(update-account
                                                          (:id account)
                                                          :name
                                                          (.. % -target -value))}]]
                [:td (count (:entries account))]

                [:td
                 [:a.has-text-danger
                  {:on-click #(remove-account (:id account))}
                  "remove"]
                 [:a
                  {:on-click (fn []
                               (select-account (:id account))
                               (pages/go-to-page :import))}
                  "import"]]])))
       [:tr [:td [:a {:on-click add-account} "+ add account"]]]]]]))
