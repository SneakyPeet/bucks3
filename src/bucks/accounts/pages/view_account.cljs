(ns bucks.accounts.pages.view-account
  (:require [re-frame.core :as rf]
            [bucks.accounts.state :as accounts]
            [bucks.accounts.core :as accounts.core]
            [bucks.shared :as shared]
            [bucks.pages.core :as pages]
            [bucks.tags.components.tag-input :as tag-input]))


(def entry-type-options
  (->> accounts.core/account-config
       (map (fn [[k v]]
              [k (map (fn [t] {:label (name t) :value t}) (:entry-types v))]))
       (into {})))


(defn page []
  (let [selected-account @(rf/subscribe [::accounts/selected-account])
        account-type (:account-type @(rf/subscribe [::accounts/account-data selected-account]))
        account-name @(rf/subscribe [::accounts/account-name selected-account])
        entries @(rf/subscribe [::accounts/account-entries selected-account])
        entry-types (get entry-type-options account-type)]
    [:div
     [shared/heading account-name
      [shared/back "back" #(do
                             (pages/go-to-page :accounts))]]
     [:div.table-container.is-size-7
      [:table.table.is-striped
       [:thead
        [:tr [:th "Date"] [:th "Description"]
         [:th "Amount"] [:th "Balance"] [:th "Exchange Rate"]
         [:th "Type"]
         [:th "Note"] [:th "Tags"]]]
       [:tbody
        (->> entries
             (map-indexed
              (fn [i {:keys [id date description amount calculated-balance exchange-rate note tags] :as entry}]
                [:tr {:key i}
                 [:td date]
                 [:td description]

                 [:td.has-text-right amount]
                 [:td calculated-balance]
                 [:td.has-text-right exchange-rate]

                 [:td [shared/select entry-types (:type entry)
                       :on-change #(accounts/update-entry-type
                                    selected-account id %)]]
                 [:td [shared/table-cell-input note
                       :placeholder "note"
                       :on-change  #(accounts/update-entry-note
                                     selected-account id %)]]
                 [:td [tag-input/tags tags
                       :placeholder "tags"
                       :on-change #(accounts/update-entry-tags
                                    selected-account id %)]]])))]]]
     [:pre (str entries)]]))
