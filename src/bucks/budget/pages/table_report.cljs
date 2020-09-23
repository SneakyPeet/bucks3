(ns bucks.budget.pages.table-report
  (:require [re-frame.core :as rf]
            [bucks.accounts.state :as accounts]
            [bucks.tags.state :as tags.state]
            [bucks.utils :as utils]
            [bucks.shared :as shared]
            [clojure.string :as string]))


(defn- entry-month [e]
  (utils/date->month (:date e)))


(defn- all-entries [accounts]
  (->>  accounts
        (map #(vals (:entries %)))
        (reduce into)))


(defn- grouped-entries [entries]
  (let [{:keys [income expense refund transfer]} (group-by :type entries)]
    {:income income
     :transfer transfer
     :expense (concat refund expense)}))


(defn- entries-by-tag-by-month [available-tags entries]
  (->> entries
       (map (fn [e]
              (let [tags (->> (:tags e)
                              sort
                              (map (fn [id]
                                     (get available-tags id))))
                    color (:color (first tags) "#efefef")
                    tag (->> (map :label tags)
                             (string/join " - "))]
                (assoc e :tag [color tag]))))
       (group-by (juxt entry-month :tag))
       (map (fn [[[month tag] entries]]
              (let [amount (->> entries
                                (map :amount-base)
                                (reduce + 0))]
                {:month month
                 :tag tag
                 :amount-base amount
                 :amount-base-p (utils/format-cents amount)})))))


(defn- budget-tbody [months entry-tag-months]
  (let [tags (->> entry-tag-months
                  (map :tag)
                  set
                  (sort-by first))
        entries (->> entry-tag-months
                     (group-by :tag)
                     (map (fn [[tag e]]
                            [tag (->> e
                                      (map (juxt :month :amount-base-p))
                                      (into {}))]))
                     (into {}))]
    [:tbody
     (->> tags
          (map-indexed
           (fn [i tag]
             (let [[color label] tag]
               [:tr {:key i}
                [:td {:style {:background-color color}}
                 label]
                (->> months
                     (map-indexed
                      (fn [j month]
                        [:td.has-text-right {:key (str i "-" j)}
                         (get-in entries [tag month])])))]))))]))

(defn- budget-totals [months entry-tag-months]
  (let [entry-months (group-by :month entry-tag-months)]
    (->> months
         (map (fn [m]
                (let [e (get entry-months m)
                      amount (->> e
                                  (map :amount-base)
                                  (reduce +))]
                  {:month m
                   :amount-base amount
                   :amount-base-p (utils/format-cents amount)}))))))

(defn- budget-totals-tbody [months budget-totals]
  [:tbody
   [:tr {:style {:border-bottom "solid 1px black"
                 :border-top "solid 1px black"}}
    [:th ]
    (->> months
         (map-indexed
          (fn [i {:keys [month amount-base-p amount-base]}]
            [:th.has-text-right
             {:key i
              :class (if (neg? amount-base)
                       "has-text-danger" "has-text-success")}
             amount-base-p])))]])


(defn- months-thead [heading months]
  [:thead
   [:tr
    [:th heading]
    (->> months
         (map-indexed
          (fn [i m]
            [:th.has-text-right {:key i} m])))]])


(defn- space []
  [:tbody
   [:tr
    [:th {:style {:height "2.5rem"}} ]]])


(defn- combined-totals [income-totals expense-totals]
  (let [e-map (->> expense-totals
                   (map (juxt :month :amount-base))
                   (into {}))]
    (->> income-totals
         (map (fn [m]
                (let [a (+ (:amount-base m) (get e-map (:month m)))]
                  (assoc m :amount-base a :amount-base-p (utils/format-cents a))))))))


(defn page []
  (let [budget-accounts @(rf/subscribe [::accounts/accounts-by-type :budget])
        available-tags @(rf/subscribe [::tags.state/available-tags-map])
        entries (-> budget-accounts all-entries grouped-entries)
        income-tag-months (entries-by-tag-by-month available-tags (:income entries))

        expense-tag-months (entries-by-tag-by-month available-tags (:expense entries))
        months (->> (concat income-tag-months expense-tag-months)
                    (map :month)
                    set
                    sort
                    reverse)
        income-budget-totals (budget-totals months income-tag-months)
        expense-budget-totals (budget-totals months expense-tag-months)
        combined-budget-totals (combined-totals income-budget-totals expense-budget-totals)]
    [:div
     [shared/table
      [months-thead "Total" months]
      [budget-totals-tbody combined-budget-totals]
      [space]
      [months-thead "Income" months]
      [budget-tbody months income-tag-months]
      [budget-totals-tbody income-budget-totals]
      [space]
      [months-thead "Expense" months]
      [budget-tbody months expense-tag-months]
      [budget-totals-tbody expense-budget-totals]]]))
