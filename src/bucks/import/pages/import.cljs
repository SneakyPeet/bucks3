(ns bucks.import.pages.import
  (:require [re-frame.core :as rf]
            [bucks.shared :as shared]
            [bucks.accounts.state :as accounts]
            [bucks.pages.core :as pages]
            [bucks.import.state :as import]
            [bucks.import.components.importer :as importer]))


(defn- receive-import [id {:keys [entries date-format header-types]}]
  (accounts/update-account id :date-format date-format)
  (accounts/update-account id :header-types header-types)
  (import/import-entries id entries)
  (pages/go-to-page :confirm-import))


(defn page []
  (let [selected-account @(rf/subscribe [::accounts/selected-account])
        account @(rf/subscribe [::accounts/account-data selected-account])
        name @(rf/subscribe [::accounts/account-name selected-account])]
    [:div
     [shared/heading "Import for " name
      [shared/back "back" #(pages/go-to-page :accounts)]]
     [importer/component account #(receive-import selected-account %)]
     #_[:pre (str (->> @(rf/subscribe [::imported-entries])
                       (apply importing/process-entries)))]
     ]))
