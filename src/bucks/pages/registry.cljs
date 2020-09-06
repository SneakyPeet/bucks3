(ns bucks.pages.registry
  (:require [re-frame.core :as rf]
            [bucks.pages.core :as pages]
            [bucks.pages.accounts :as accounts]))


(defn init-state [state]
  (assoc state ::pages/current-page :accounts))


(defn component []
  (let [current-page @(rf/subscribe [::pages/current-page])]
    (case current-page
      :accounts [accounts/page]
      :import [accounts/import-page]
      :confirm-import [accounts/comfirm-import-page]
      :view-account [accounts/view-account]
      [:div.has-text-danger
       "Page not defined: " (str current-page)])))
