(ns bucks.options.pages.manage
  (:require [bucks.options.state :as options]
            [re-frame.core :as rf]
            [bucks.options.components.select-currency :as select-currency]))


(defn page []
  (let [{:keys [base-currency fixer-api-key] :as o} @(rf/subscribe [::options/options])]
    [:div
     [:pre (str o)]
     [select-currency/component base-currency (fn [v] (options/set-option :base-currency v))]]))
