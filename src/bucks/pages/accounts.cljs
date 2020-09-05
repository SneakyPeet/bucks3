(ns bucks.pages.accounts
  (:require [re-frame.core :as rf]
            [bucks.accounts.core :as accounts]
            [bucks.accounts.importing :as importing]
            [bucks.import.core :as importer]
            [bucks.pages.core :as pages]
            [bucks.shared :as shared]))


(rf/reg-event-db
 ::import-entries
 (fn [db [_ id new-entries]]
   (assoc db ::imported-entries
          (importing/process-entries
           (get-in db [::accounts/accounts id :entries])
           new-entries))))

(rf/reg-sub
 ::imported-entries
 (fn [db _]
   (::imported-entries db)))


(rf/reg-event-db
 ::finish-import
 (fn [db _]
   (dissoc db ::imported-entries)))


(defn page []
  [:div
   [:h1.heading "Accounts"]
   [accounts/component]])


(defn- receive-import [id {:keys [entries date-format header-types]}]
  (accounts/update-account id :date-format date-format)
  (accounts/update-account id :header-types header-types)
  (rf/dispatch [::import-entries id entries])
  (pages/go-to-page :confirm-import))


(defn import-page []
  (let [selected-account @(rf/subscribe [::accounts/selected-account])
        account @(rf/subscribe [::accounts/account-data selected-account])
        name (if (empty? (:name account)) "[account name missing]" (:name account))]
    [:div
     [shared/heading "Import for " name
      [:small.is-pulled-right [:a {:on-click #(pages/go-to-page :accounts)} "back"]]]
     [importer/component account #(receive-import selected-account %)]
     [:pre (str (->> @(rf/subscribe [::imported-entries])
                     (apply importing/process-entries)))]
     ]))


(defn comfirm-import-page []
  (let [selected-account @(rf/subscribe [::accounts/selected-account])
        imported @(rf/subscribe [::imported-entries])]
    [:div
     [shared/heading "Confirm Import"
      [:small.is-pulled-right
       [:a {:on-click (fn []
                        (pages/go-to-page :accounts)
                        (rf/dispatch [::finish-import]))} "cancel"]]]
     [:div.table-container.is-size-7
      [:table.table.is-striped
       [:thead
        [:tr [:th "date"] [:th "description"]
         [:th.has-text-right "amount"]
         [:th.has-text-right "balance"]]]
       [:tbody
        (->> imported
             (map-indexed
              (fn [i {:keys [id description amount balance date duplicate? existing?] :as e}]
                [:tr {:key i
                      :class (cond
                               existing? "has-background-info"
                               duplicate? "has-background-warning"
                               :else "")}
                 [:td date]
                 [:td description]
                 [:td.has-text-right amount]
                 [:td.has-text-right balance]])))]]]]))
