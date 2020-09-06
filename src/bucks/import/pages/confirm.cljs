(ns bucks.import.pages.confirm
  (:require [re-frame.core :as rf]
            [bucks.accounts.state :as accounts]
            [bucks.pages.core :as pages]
            [bucks.import.state :as import]
            [bucks.shared :as shared]))


(defn page []
  (let [selected-account @(rf/subscribe [::accounts/selected-account])
        imported @(rf/subscribe [::import/imported-entries])
        clear (fn []
                (pages/go-to-page :accounts)
                (import/finish-import))
        complete (fn []
                   (import/confirm-entries selected-account imported)
                   (clear))]
    [:div
     [shared/heading "Confirm Import"
      [shared/back "cancel" clear ]]

     [:div.table-container.is-size-7
      [:table.table.is-striped
       [:thead
        [:tr [:th "date"] [:th "description"]
         [:th.has-text-right "amount"]
         [:th.has-text-right "balance"]
         [:th "status"]]]
       [:tbody
        (->> imported
             (map-indexed
              (fn [i {:keys [id description amount balance date duplicate? existing?] :as e}]
                [:tr {:key i
                      :class (cond
                               existing? ""
                               duplicate? "has-background-warning"
                               :else "has-background-primary")}
                 [:td date]
                 [:td description]
                 [:td.has-text-right amount]
                 [:td.has-text-right balance]
                 [:td (cond (:existing? e) ""
                            (:duplicate? e) "duplicate"
                            :else "new")]
                 ])))]]]
     [:button.button.is-small.is-primary
      {:on-click complete}
      "accept import"]]))
