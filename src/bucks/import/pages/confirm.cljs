(ns bucks.import.pages.confirm
  (:require [re-frame.core :as rf]
            [bucks.accounts.state :as accounts]
            [bucks.pages.core :as pages]
            [bucks.import.state :as import]
            [bucks.shared :as shared]
            [bucks.options.state :as opts]
            [bucks.components.fixer-io :as fixer]))



(defn import-exchange-rates [access-key base-currency account-currency entries]
  (import/clear-exchange-rate-error)
  (let [dates (->> entries
                   (filter #(not (or (:existing? %) (:duplicate? %))))
                   (map :date)
                   set)]
    (doseq [date dates]
      (fixer/fetch-rate :access-key access-key
                        :date date
                        :base-currency base-currency
                        :destination-currency account-currency
                        :success #(import/set-exchange-rate base-currency account-currency date %)
                        :fail import/set-exchange-rate-error))))


(defn page []
  (let [selected-account @(rf/subscribe [::accounts/selected-account])
        account-currency (:currency @(rf/subscribe [::accounts/account-data selected-account]))
        {:keys [base-currency fixer-api-key]} @(rf/subscribe [::opts/options])
        same-currency? (= base-currency account-currency)
        exchange-rates @(rf/subscribe [::import/exchange-rates base-currency account-currency])
        currency-for-date (fn [d] (get exchange-rates d 1))
        exchange-rate-err @(rf/subscribe [::import/exchange-rate-error])
        imported @(rf/subscribe [::import/imported-entries])
        update-exchange-rates #(import-exchange-rates fixer-api-key base-currency account-currency imported)
        clear (fn []
                (pages/go-to-page :accounts)
                (import/finish-import))
        complete (fn []
                   (import/confirm-entries selected-account imported)
                   (clear))]
    [:div
     [shared/heading "Confirm Import"
      [shared/back "cancel" clear ]]

     (when-not same-currency?
       [:div
        [:p "Account currency differs from base currency. Click to import from fixer.io."]
        [:div base-currency "|" account-currency]
        [:div.buttons [:button.button.is-primary.is-small
                       {:on-click update-exchange-rates}
                       "Update exchange rates"]]
        (when exchange-rate-err
          [:div.notification.is-warning
           (:code exchange-rate-err) " - " (:info exchange-rate-err)])
        [:hr]])

     [:div.table-container.is-size-7
      [:table.table.is-striped
       [:thead
        [:tr [:th "date"] [:th "description"]
         [:th.has-text-right "amount"]
         [:th.has-text-right "balance"]
         [:th "status"]
         (when-not same-currency?
           [:th base-currency "|" account-currency])]]
       [:tbody
        (->> imported
             (map-indexed
              (fn [i {:keys [id description amount balance date duplicate? existing?] :as e}]
                (let [new? (not (or existing? duplicate?))]
                  [:tr {:key i
                        :class (cond
                                 existing? ""
                                 duplicate? "has-background-warning"
                                 :else "has-background-primary")}
                   [:td date]
                   [:td description]
                   [:td.has-text-right amount]
                   [:td.has-text-right balance]
                   [:td (cond existing? ""
                              duplicate? "duplicate"
                              new? "new")]
                   (when-not same-currency?
                     [:td.has-text-centered
                      (when new?
                        (str "1:" (str (currency-for-date date))))])
                   ]))))]]]
     [:button.button.is-small.is-primary
      {:on-click complete}
      "accept import"]]))
