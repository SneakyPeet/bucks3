(ns bucks.accounts.components.entries
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


(defn component [account-id entries]
  (let [account-type (:account-type @(rf/subscribe [::accounts/account-data account-id]))
        entry-types (get entry-type-options account-type)]
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
                                   account-id id %)]]
                [:td [shared/table-cell-input note
                      :placeholder "note"
                      :on-change  #(accounts/update-entry-note
                                    account-id id %)]]
                [:td [tag-input/tags tags
                      :placeholder "tags"
                      :on-change #(accounts/update-entry-tags
                                   account-id id %)]]])))]]]))
