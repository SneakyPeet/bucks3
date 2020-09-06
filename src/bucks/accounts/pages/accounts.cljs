(ns bucks.accounts.pages.accounts
  (:require [re-frame.core :as rf]
            [bucks.shared :as shared]
            [bucks.accounts.state :as accounts]
            [bucks.pages.core :as pages]))


(defn page []
  (let [accounts @(rf/subscribe [::accounts/accounts])]
    [:div
     [shared/heading "Accounts"]
     [shared/table
      (when-not (empty? accounts)
        [:thead
         [:tr [:td "Accounts"] [:td] [:td]]])
      [:tbody
       (->> accounts
            (map-indexed
             (fn [i account]
               [:tr {:key i}
                [:td [:input.input.is-small {:value (:name account)
                                             :placeholder "I need a name"
                                             :on-change #(accounts/update-account
                                                          (:id account)
                                                          :name
                                                          (.. % -target -value))}]]
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
                  "view "]]])))
       [:tr [:td [:a {:on-click accounts/add-account} "+ add account"]]]]]]))
