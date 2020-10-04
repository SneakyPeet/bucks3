(ns bucks.budget.core)


(defn budget-item [id group color & {:keys [stat amount-base]
                                     :or {stat :none amount-base 0}}]
  {:id id :group group :color color :stat stat :amount-base amount-base})


(defn set-stat [item stat amount-base]
  (assoc item :stat stat :amount-base amount-base))
