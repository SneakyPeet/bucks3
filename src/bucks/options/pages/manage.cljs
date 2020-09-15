(ns bucks.options.pages.manage
  (:require [bucks.options.state :as options]
            [re-frame.core :as rf]
            [bucks.options.components.select-currency :as select-currency]))


(defn page []
  (let [{:keys [base-currency fixer-api-key] :as o} @(rf/subscribe [::options/options])]
    [:div {:style {:max-width "250px"}}
     [:div.field
      [:label.label "Base Currency"]
      [:div.control
       [select-currency/component base-currency (fn [v] (options/set-option :base-currency v))]]
      [:div.help.is-warning "Changing this after you have imported entries with different exchange rates will cause undesirable behaviour"]]
     [:div.field
      [:label.label "Fixer Api Access Key"]
      [:div.control
       [:input.input {:value fixer-api-key
                      :on-change #(options/set-option :fixer-api-key (.. % -target -value))}]]
      [:div.help "Use " [:a {:href "https://fixer.io/" :target "_blank"} "fixer.io"]
       " for syncing exchange rates. Free account is sufficient."]]]))
