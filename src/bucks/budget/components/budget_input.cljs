(ns bucks.budget.components.budget-input
  (:require [re-frame.core :as rf]
            [bucks.budget.state :as budget]
            [bucks.budget.core :as budget.core]
            [bucks.utils :as utils]))


(defn input [budget-item]
  (let [input-enabled? @(rf/subscribe [::budget/input-enabled?])
        amount-base (:amount-base budget-item)]
    (if input-enabled?
      (let [v (/ amount-base 100)]
        [:input.input.is-small
         {:type "number"
          :style {:min-width "6rem"}
          :default-value v
          :on-change (fn [e]
                       (let [v (-> (.. e -target -value)
                                   js/parseFloat
                                   (* 100)
                                   (Math/round))]
                         (budget/set-budget-item
                          (budget.core/set-stat budget-item :custom v))))}])
      (when-not (or (zero? amount-base) (nil? amount-base))
        [:span
         {:style {:color
                  (case (:stat budget-item)
                    :none "#0065ff"
                    :custom "#880eaf"
                    "#72beff")}}
         (utils/format-cents (:amount-base budget-item))]))))


(defn toggle []
  (let [input-enabled? @(rf/subscribe [::budget/input-enabled?])]
    [:button.button.is-small
     {:on-click #(budget/toggle-enable-input)}
     (if input-enabled? "Disable" "Enable") " input"]))
