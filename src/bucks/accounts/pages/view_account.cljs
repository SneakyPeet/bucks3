(ns bucks.accounts.pages.view-account
  (:require [re-frame.core :as rf]
            [bucks.accounts.state :as accounts]
            [bucks.accounts.core :as accounts.core]
            [bucks.shared :as shared]
            [bucks.pages.core :as pages]
            [bucks.tags.components.tag-input :as tag-input]
            [bucks.accounts.components.entries :as entries.c]
            [bucks.accounts.components.heading :as account.heading]))


(defn page []
  (let [selected-account @(rf/subscribe [::accounts/selected-account])
        account-name @(rf/subscribe [::accounts/account-name selected-account])
        entries @(rf/subscribe [::accounts/account-entries selected-account])]
    [:div
     [account.heading/component]
     [entries.c/component selected-account entries]]))
