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
       [:tr [:th "import time"] [:th "from"] [:th "to"] [:th "total"] [:th "need tags"] [:th "actions"]]]
      [:tbody
       (->> entries
            (group-by :import-id)
            (sort-by first)
            reverse
            (map-indexed
             (fn [i [k e]]
               (let [[from to] (entry-range e)]
                 [:tr {:key i}
                  [:td (imports.core/import-id->str k)]
                  [:td from]
                  [:td to]
                  [:td (count e)]
                  [:td (accounts.core/entries-missing-tags-total e)]
                  [:td
                   [:a.has-text-danger
                    {:on-click #(when (js/confirm "You cannot undo import removal! Proceed?")
                                  (accounts/remove-entries
                                   selected-account
                                   (fn [e]
                                     (= k (:import-id e)))))}
                    "remove "]
                   [:a
                    {:on-click (fn []
                                 (imports/select-import k)
                                 (pages/go-to-page :view-import))}
                    "view "]]]))))]]]))
