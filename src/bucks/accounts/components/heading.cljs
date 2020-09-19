(ns bucks.accounts.components.heading
  (:require [re-frame.core :as rf]
            [bucks.shared :as shared]
            [bucks.pages.core :as pages]
            [bucks.accounts.state :as accounts]))


(defn component [& {:keys [back-page sub-heading]
                    :or {back-page :accounts}}]
  (let [selected-account @(rf/subscribe [::accounts/selected-account])
        account-name @(rf/subscribe [::accounts/account-name selected-account])]
    [shared/heading
     (if sub-heading (str account-name " | " sub-heading) account-name)
     [shared/back "back"
      #(pages/go-to-page back-page)]]))
