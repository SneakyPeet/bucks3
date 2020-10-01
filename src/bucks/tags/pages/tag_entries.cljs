(ns bucks.tags.pages.tag-entries
  (:require [re-frame.core :as rf]
            [bucks.tags.state :as tags.state]
            [bucks.options.state :as options]
            [bucks.shared :as shared]))


(defn page []
  (let [tag @(rf/subscribe [::tags.state/selected-tag])
        entries @(rf/subscribe [::tags.state/tag-entries])
        base-currency (:base-currency @(rf/subscribe [::options/options]))]
    [:div
     [shared/heading "TAG: " (:label tag)
      [shared/back "back"]]
     [shared/table
      [:tbody
       (->> entries
            (map-indexed
             (fn [i {:keys [date account-name description note currency amount amount-p amount-base-p]}]
               [:tr {:key i}
                [:td date]
                [:td account-name]
                [:td description]
                [:td note]
                [:td.has-text-right
                 [:span
                  {:class (when-not (neg? amount) "has-text-primary")}
                  amount-p " " currency]
                 (when-not (= base-currency currency)
                   [:div [:small.has-text-grey amount-base-p " " base-currency]])]])))]]]))
