(ns bucks.accounts.pages.view-account
  (:require [re-frame.core :as rf]
            [bucks.accounts.state :as accounts]
            [bucks.shared :as shared]
            [bucks.pages.core :as pages]))


(defn page []
  (let [selected-account @(rf/subscribe [::accounts/selected-account])
        account-name @(rf/subscribe [::accounts/account-name selected-account])
        entries @(rf/subscribe [::accounts/account-entries selected-account])]
    [:div
     [shared/heading account-name
      [shared/back "back" #(do
                             (pages/go-to-page :accounts))]]
     [:div.table-container.is-size-7
      [:table.table.is-striped
       [:thead]
       [:tbody
        (->> entries
             (map-indexed
              (fn [i {:keys [date description amount]}]
                [:tr {:key i}
                 [:td date]
                 [:td description]
                 [:td.has-text-right amount]])))]]]
     [:pre (str entries)]]))
