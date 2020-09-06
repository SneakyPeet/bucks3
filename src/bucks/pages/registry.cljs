(ns bucks.pages.registry
  (:require [re-frame.core :as rf]
            [bucks.pages.core :as pages]
            [bucks.accounts.pages.accounts :as accounts]
            [bucks.import.pages.import :as import]
            [bucks.import.pages.confirm :as confirm-import]
            [bucks.accounts.pages.view-account :as view-account]))

(defn init-state [state]
  (assoc state ::pages/current-page :accounts))


(defn component []
  (let [current-page @(rf/subscribe [::pages/current-page])]
    (case current-page
      :accounts [accounts/page]
      :import [import/page]
      :confirm-import [confirm-import/page]
      :view-account [view-account/page]
      [:div.has-text-danger
       "Page not defined: " (str current-page)])))
