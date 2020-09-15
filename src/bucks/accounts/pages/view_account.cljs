(ns bucks.accounts.pages.view-account
  (:require [re-frame.core :as rf]
            [bucks.accounts.state :as accounts]
            [bucks.shared :as shared]
            [bucks.pages.core :as pages]
            [bucks.tags.components.tag-input :as tag-input]))


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
       [:thead
        [:tr [:th "Date"] [:th "Description"] [:th "Amount"] [:th "Balance"] [:th "Exchange Rate"] [:th "Note"] [:th "Tags"]]]
       [:tbody
        (->> entries
             (map-indexed
              (fn [i {:keys [id date description amount calculated-balance exchange-rate note tags]}]
                [:tr {:key i}
                 [:td date]
                 [:td description]
                 [:td.has-text-right amount]
                 [:td calculated-balance]
                 [:td.has-text-right exchange-rate]
                 [:td [shared/table-cell-input note
                       :placeholder "note"
                       :on-change  #(accounts/update-entry-note
                                     selected-account id %)]]
                 [:td [tag-input/tags tags
                       :placeholder "tags"
                       :on-change #(accounts/update-entry-tags
                                    selected-account id %)]]])))]]]
     [:pre (str entries)]]))
