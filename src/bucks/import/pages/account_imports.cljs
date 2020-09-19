(ns bucks.import.pages.account-imports
  (:require [re-frame.core :as rf]
            [bucks.accounts.state :as accounts]
            [bucks.accounts.components.heading :as account.heading]
            [bucks.shared :as shared]))


(defn page []
  (let [selected-account @(rf/subscribe [::accounts/selected-account])
        entries @(rf/subscribe [::accounts/account-entries selected-account])]
    [:div
     [account.heading/component :sub-heading "imports"]
     [shared/table
      [:thead
       [:tr [:td "Import time"] [:td "Entries"] [:td]]]
      [:tbody
       (->> entries
            (group-by :import-id)
            (sort-by first)
            reverse
            (map-indexed
             (fn [i [k e]]
               [:tr {:key i}
                [:td (.toLocaleString (js/Date. k))]
                [:td (count e)]
                [:td
                #_ [:a.has-text-danger
                  #_{:on-click #(accounts/remove-account (:id account))}
                  "remove "]
                 [:a
                 #_ {:on-click (fn []
                               (accounts/select-account (:id account))
                               (pages/go-to-page :import))}
                  "view "]]])))]]]))
