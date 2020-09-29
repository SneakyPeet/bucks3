(ns bucks.budget.components.budget-info
  (:require [re-frame.core :as rf]
            [bucks.budget.state :as budget]
            [bucks.utils :as utils]
            [bucks.shared :as shared]))




(defn budget-info []
  (let [budget (->> @(rf/subscribe [::budget/current-budget])
                    (vals)
                    (map :amount-base)
                    (group-by pos?)
                    (map (fn [[p? e]]
                           [p? (reduce + 0 e)]))
                    (into {}))
        income (get budget true)
        expense (get budget false)
        total (+ income expense)
        n? (neg? total)]
    [:div.level
     [shared/level-item "income" (str "+" (utils/format-cents income)) :title-class "has-text-success"]
     [shared/level-item "expense" (utils/format-cents expense) :title-class "has-text-danger"]
     [shared/level-item "budget"
      (str "="
           (utils/format-cents total)) :title-class (if n?
                                                      "has-text-danger"
                                                      "has-text-success")]]))
