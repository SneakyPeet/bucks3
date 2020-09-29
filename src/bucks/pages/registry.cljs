(ns bucks.pages.registry
  (:require [re-frame.core :as rf]
            [bucks.pages.core :as pages]
            [bucks.accounts.pages.accounts :as accounts]
            [bucks.accounts.pages.view-account :as view-account]
            [bucks.import.pages.import :as import]
            [bucks.import.pages.confirm :as confirm-import]
            [bucks.import.pages.account-imports :as account-imports]
            [bucks.tags.pages.manage :as manage-tags]
            [bucks.options.pages.manage :as options]
            [bucks.import.pages.view-import :as view-import]
            [bucks.budget.pages.table-report :as budget-table-report]))

(defn init-state [state]
  (assoc state ::pages/current-page :accounts))


(def menu-items
  {:accounts "accounts"
   :manage-tags "tags"
   :manage-options "options"
   :budget-table "budget:report"})


(defn- wrap-menu [c]
  (let [current-page @(rf/subscribe [::pages/current-page])]
    [:div
     [:div.tabs
      [:ul
       (->> menu-items
            (map-indexed
             (fn [i [k t]]
               [:li {:key i :class (when (= current-page k)
                                     "is-active")}
                [:a.heading {:on-click #(pages/go-to-page k)} t]])))]]
     c]))


(defn component []
  (let [current-page @(rf/subscribe [::pages/current-page])]
    (case current-page
      :accounts [wrap-menu [accounts/page]]
      :view-account [view-account/page]

      :import [import/page]
      :confirm-import [confirm-import/page]
      :account-imports [account-imports/page]
      :view-import [view-import/page]

      :manage-tags [wrap-menu [manage-tags/page]]
      :manage-options [wrap-menu [options/page]]

      :budget-table [wrap-menu [budget-table-report/page]]
      [:div.has-text-danger
       "Page not defined: " (str current-page)])))
