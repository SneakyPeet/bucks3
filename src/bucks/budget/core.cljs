(ns bucks.budget.core
  (:require [clojure.string :as string]))


(defn group-tags [available-tags tags]
  (let [tags (->> tags
                  sort
                  (map (fn [id]
                         (get available-tags id))))
        color (:color (first tags) "black")
        tag (->> (map :label tags)
                 sort
                 (string/join " - "))]
    {:tag tag :color color}))


(defn budget-item [id group color & {:keys [stat amount-base]
                                     :or {stat :none amount-base 0}}]
  {:id id :group group :color color :stat stat :amount-base amount-base})


(defn set-stat [item stat amount-base]
  (assoc item :stat stat :amount-base amount-base))
