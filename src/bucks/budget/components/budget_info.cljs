(ns bucks.budget.components.budget-info
  (:require [re-frame.core :as rf]
            [bucks.budget.state :as budget]
            [bucks.utils :as utils]
            [bucks.shared :as shared]))


(defn budget-info []
  (let [{:keys
         [income
          expense
          savings]} (->> @(rf/subscribe [::budget/current-budget])
         (map (fn [[k e]]
                [k (->> (vals e)
                        (map :amount-base)
                        (reduce + 0))]))
         (into {}))

        total (+ income (- savings) expense)
        n? (neg? total)

        ]
    [:div.level
     [shared/level-item "income" (str "+" (utils/format-cents income)) :title-class "has-text-success"]
     [shared/level-item "bucket" (utils/format-cents (- savings)) :title-class "has-text-warning"]
     [shared/level-item "expense" (utils/format-cents expense) :title-class "has-text-danger"]
     [shared/level-item "budget"
      (str "="
           (utils/format-cents total)) :title-class (if n?
                                                      "has-text-danger"
                                                      "has-text-success")]]))
