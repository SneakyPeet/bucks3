(ns bucks.import.pages.view-import
  (:require [re-frame.core :as rf]
            [bucks.accounts.state :as accounts]
            [bucks.import.state :as import]
            [bucks.import.core :as import.core]
            [bucks.accounts.components.heading :as heading]
            [bucks.accounts.components.entries :as entries.c]))


(defn page []
  (let [selected-account @(rf/subscribe [::accounts/selected-account])
        selected-import @(rf/subscribe [::import/selected-import])
        account-name @(rf/subscribe [::accounts/account-name selected-account])
        entries @(rf/subscribe [::accounts/account-entries selected-account])]
    [:div
     [heading/component
      :sub-heading (str "import | " (import.core/import-id->str selected-import))
      :back-page :account-imports]
     [entries.c/component selected-account (->> entries
                                                (filter #(= selected-import (:import-id %))))]]))
