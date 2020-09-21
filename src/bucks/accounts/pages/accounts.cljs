(ns bucks.accounts.pages.accounts
  (:require [re-frame.core :as rf]
            [bucks.shared :as shared]
            [bucks.accounts.state :as accounts]
            [bucks.accounts.core :as accounts.core]
            [bucks.pages.core :as pages]
            [bucks.options.components.select-currency :as select-currency]
            [bucks.options.state :as options]))


(defn page []
  (let [accounts @(rf/subscribe [::accounts/accounts])
        base-currency (:base-currency @(rf/subscribe [::options/options]))]
    [:div
     [shared/table
      (when-not (empty? accounts)
        [:thead
         [:tr [:th "Accounts"] [:th "Balance"]
          [:th "Currency"] [:th "Type"]
           [:th "Total entries"] [:th "Need tags"]
          [:th "Actions"]
          [:th "View"]]])
      [:tbody
       (->> accounts
            (map-indexed
             (fn [i account]
               (let [total-entries (count (:entries account))
                     update-account (fn [k v]
                                      (accounts/update-account (:id account) k v))
                     cant-change? (> total-entries 0)
                     currency (:currency account)
                     same-as-base? (= currency base-currency)]
                 [:tr {:key i}
                  [:td [:input.input.is-small {:value (:name account)
                                               :placeholder "I need a name"
                                               :on-change #(update-account
                                                            :name
                                                            (.. % -target -value))}]]
                  [:td (:current-balance-p account)
                   (when-not same-as-base?
                     [:div [:small.has-text-grey (:current-balance-base-p account) " " base-currency]])]
                  [:td (if cant-change?
                         currency
                         [select-currency/component currency #(update-account :currency %)])]
                  [:td (if cant-change?
                         (name (:account-type account))
                         [:div.field.has-addons
                          (->> accounts.core/account-types
                               (map-indexed
                                (fn [i t]
                                  (let [n (str (name t))]
                                    [:div.control
                                     [:button.button.is-small
                                      {:class (when (= t (:account-type account)) "is-primary")
                                       :on-click #(update-account :account-type t)}
                                      n]]))))])]

                  [:td total-entries]
                  [:td (accounts.core/entries-missing-tags-total (vals (:entries account)))]

                  [:td
                   [:a.has-text-danger
                    {:on-click #(accounts/remove-account (:id account))}
                    "remove "]
                   [:a
                    {:on-click (fn []
                                 (accounts/select-account (:id account))
                                 (pages/go-to-page :import))}
                    "import "]]
                  [:td
                   [:a.has-text-success
                    {:on-click (fn []
                                 (accounts/select-account (:id account))
                                 (pages/go-to-page :view-account))}
                    "entries "]

                   [:a.has-text-warning
                    {:on-click (fn []
                                 (accounts/select-account (:id account))
                                 (pages/go-to-page :account-imports))}
                    "imports "]]]))))
       [:tr [:td [:a {:on-click accounts/add-account} "+ add account"]]]]]]))
