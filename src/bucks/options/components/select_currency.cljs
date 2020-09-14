(ns bucks.options.components.select-currency
  (:require [bucks.shared :as shared]
            [bucks.options.core :as options.core]))


(defn component [value on-change]
  (let [currencies (->> options.core/supported-currencies
                        (map (fn [[k v]]
                               {:value k
                                :label (str k " - " v)})))]
    [shared/select currencies value
     {:on-change on-change}]
    ))
