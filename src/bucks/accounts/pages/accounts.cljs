(ns bucks.accounts.pages.accounts
  (:require [re-frame.core :as rf]
            [bucks.shared :as shared]
            [bucks.accounts.state :as accounts]
            [bucks.pages.core :as pages]
            [bucks.options.components.select-currency :as select-currency]))


(defn page []
  (let [accounts @(rf/subscribe [::accounts/accounts])]
    [:div
     [shared/table
      (when-not (empty? accounts)
        [:thead
         [:tr [:td "Accounts"] [:td "Currency"] [:td "Balance"] [:td "Total entries"] [:td "Actions"]]])
      [:tbody
       (->> accounts
            (map-indexed
             (fn [i account]
               (let [update-account (fn [k v]
                                      (accounts/update-account (:id account) k v))]
                 [:tr {:key i}
                  [:td [:input.input.is-small {:value (:name account)
                                               :placeholder "I need a name"
                                               :on-change #(update-account
                                                            :name
                                                            (.. % -target -value))}]]
                  [:td [select-currency/component (:currency account) #(update-account :currency %)]]
                  [:td (:current-balance account)]
                  [:td (count (:entries account))]

                  [:td
                   [:a.has-text-danger
                    {:on-click #(accounts/remove-account (:id account))}
                    "remove "]
                   [:a
                    {:on-click (fn []
                                 (accounts/select-account (:id account))
                                 (pages/go-to-page :import))}
                    "import "]
                   [:a.has-text-success
                    {:on-click (fn []
                                 (accounts/select-account (:id account))
                                 (pages/go-to-page :view-account))}
                    "view "]]]))))
       [:tr [:td [:a {:on-click accounts/add-account} "+ add account"]]]]]]))
