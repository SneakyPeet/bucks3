(ns bucks.budget.components.clear-budget
  (:require [bucks.budget.state :as budget]))


(defn clear []
  [:button.button.is-danger.is-small
   {:on-click (fn []
                (when (js/confirm "Clear?")
                  (budget/clear-current-budget)))}
   "Clear Budget"])
