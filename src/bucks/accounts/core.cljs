(ns bucks.accounts.core
  (:require [re-frame.core :as rf]))

(defn init-state [s]
  (assoc s ::acounts {}))


(defn new-account [id]
  {:id id
   :name ""
   :date-format nil
   :header-types {}
   :transactions []})


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


(defn- add-account []
  (rf/dispatch [::add-account (str (random-uuid))]))


(defn- remove-account [id]
  (when (js/confirm "You cannot undo account removal! Proceed?")
    (rf/dispatch [::remove-account id])))


(defn- update-account [id path value]
  (rf/dispatch [::update-account id
                (if (keyword? path) [path] path)
                value]))


(defn component []
  (let [accounts @(rf/subscribe [::accounts])]
    [:div
     [:pre accounts]
     [:table.table.is-small
      (when-not (empty? accounts)
        [:thead
         [:tr [:td "Accounts"] [:td]]])
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

                [:td [:a.has-text-danger
                      {:on-click #(remove-account (:id account))}
                      "remove"]]])))
       [:tr [:td [:a {:on-click add-account} "+ add account"]]]]]]))
