(ns bucks.pages.registry
  (:require [re-frame.core :as rf]
            [bucks.pages.core :as pages]
            [bucks.accounts.pages.accounts :as accounts]
            [bucks.import.pages.import :as import]
            [bucks.import.pages.confirm :as confirm-import]
            [bucks.accounts.pages.view-account :as view-account]
            [bucks.tags.pages.manage :as manage-tags]
            [bucks.options.pages.manage :as options]))

(defn init-state [state]
  (assoc state ::pages/current-page :accounts))


(def menu-items
  {:accounts "accounts"
   :manage-tags "tags"
   :manage-options "options"})


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
      :import [import/page]
      :confirm-import [confirm-import/page]
      :view-account [view-account/page]
      :manage-tags [wrap-menu [manage-tags/page]]
      :manage-options [wrap-menu [options/page]]
      [:div.has-text-danger
       "Page not defined: " (str current-page)])))
