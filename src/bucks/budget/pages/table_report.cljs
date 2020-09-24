(ns bucks.budget.pages.table-report
  (:require [re-frame.core :as rf]
            [bucks.accounts.state :as accounts]
            [bucks.tags.state :as tags.state]
            [bucks.utils :as utils]
            [bucks.shared :as shared]
            [clojure.string :as string]
            [cljs-bean.core :refer [->js]]
            ["simple-statistics" :refer (mean quantile)]))


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


(defn- budget-tbody [months entry-tag-months stats]
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
             (let [[color label] tag
                   stats' (get stats tag)]
               [:tr {:key i}
                [:td {:style {:background-color color}}
                 label]
                (->> stats'
                     (map-indexed
                      (fn [i [k v]]
                        [:td.has-text-right {:key i} (:amount-base-p v)])))
                #_[:td.has-text-right (get-in stats [tag :avg :amount-base-p])]
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


(defn- budget-totals-tbody [stats months]
  [:tbody
   [:tr {:style {:border-bottom "solid 1px black"
                 :border-top "solid 1px black"}}
    [:th]

    (->> stats
         first last
         (map-indexed
          (fn [i _]
            [:th {:key i}])))

    (->> months
         (map-indexed
          (fn [i {:keys [month amount-base-p amount-base]}]
            [:th.has-text-right
             {:key i
              :class (if (neg? amount-base)
                       "has-text-danger" "has-text-success")}
             amount-base-p])))]])


(defn- months-thead [heading stats months]
  [:thead
   [:tr
    [:th heading]
    (->> stats
         first last
         (map-indexed
          (fn [i [k _]]
            [:th.has-text-right {:key i} (name k)])))
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


(defn- stats [months entry-tag-month]
  (let [total-months (count months)]
    (if (zero? total-months)
      {}
      (->> entry-tag-month
           (group-by :tag)
           (map
            (fn [[tag e-months]]
              (let [->amounts (fn [n] {:amount-base n
                                       :amount-base-p (utils/format-cents n)})
                    pad-zeros (repeat (- total-months (count e-months)) 0)
                    amounts' (into (map :amount-base e-months)
                                   pad-zeros)
                    amounts (->js amounts')
                    mean (mean amounts)
                    [q70 min-max] (if (> mean 0)
                                     [0.7 max]
                                     [0.3 min])
                    stats' {:max (apply min-max amounts')
                            :mean mean
                            :q70 (quantile amounts q70)}]
                [tag (->> stats'
                          (map (fn [[k v]] [k (->amounts v)]))
                          (into {}))])))
           (into {}))
      )))


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
        income-stats (stats months income-tag-months)
        expense-stats (stats months expense-tag-months)
        income-budget-totals (budget-totals months income-tag-months)
        expense-budget-totals (budget-totals months expense-tag-months)
        combined-budget-totals (combined-totals income-budget-totals expense-budget-totals)]
    [:div
     [:pre (str (stats months income-tag-months))]
     [shared/table
      [months-thead "Total" income-stats months]
      [budget-totals-tbody income-stats combined-budget-totals]
      [space]
      [months-thead "Income" income-stats months]
      [budget-tbody months income-tag-months income-stats]
      [budget-totals-tbody income-stats income-budget-totals]
      [space]
      [months-thead "Expense" expense-stats months]
      [budget-tbody months expense-tag-months expense-stats]
      [budget-totals-tbody income-stats expense-budget-totals]]]))
