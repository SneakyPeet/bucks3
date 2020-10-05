(ns bucks.import.pages.account-imports
  (:require [re-frame.core :as rf]
            [bucks.accounts.state :as accounts]
            [bucks.accounts.components.heading :as account.heading]
            [bucks.accounts.core :as accounts.core]
            [bucks.shared :as shared]
            [bucks.import.state :as imports]
            [bucks.import.core :as imports.core]
            [bucks.pages.core :as pages]))

(defn- entry-range [entries]
  (let [dates (->> entries
                   (map :date)
                   (sort))]
    [(first dates) (last dates)]))

(defn page []
  (let [selected-account @(rf/subscribe [::accounts/selected-account])
        entries @(rf/subscribe [::accounts/account-entries selected-account])]
    [:div
     [account.heading/component :sub-heading "imports"]
     [shared/table
      [:thead
       [:tr [:th "from"] [:th "to"] [:th "import time"] [:th "total"] [:th "need tags"] [:th "actions"]]]
      [:tbody
       (->> entries
            (group-by :import-id)
            (map (fn [[id e]]
                   (let [[from to] (entry-range e)]
                     {:import-id id
                      :date (imports.core/import-id->str id)
                      :total (count e)
                      :missing-tags (accounts.core/entries-missing-tags-total e)
                      :from from
                      :to to})))
            (sort-by :from)
            reverse
            (map-indexed
             (fn [i {:keys [import-id date total missing-tags from to]}]
               [:tr {:key i}
                [:td from]
                [:td to]
                [:td date]
                [:td total]
                [:td missing-tags]
                [:td
                 [:a.has-text-danger
                  {:on-click #(when (js/confirm "You cannot undo import removal! Proceed?")
                                (accounts/remove-entries
                                 selected-account
                                 (fn [e]
                                   (= import-id (:import-id e)))))}
                  "remove "]
                 [:a
                  {:on-click (fn []
                               (imports/select-import import-id)
                               (pages/go-to-page :view-import))}
                  "view "]]])))]]]))
