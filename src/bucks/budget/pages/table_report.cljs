(ns bucks.budget.pages.table-report
  (:require [re-frame.core :as rf]
            [bucks.accounts.state :as accounts]
            [bucks.tags.state :as tags.state]
            [bucks.utils :as utils]
            [bucks.shared :as shared]
            [clojure.string :as string]
            [cljs-bean.core :refer [->js]]
            [bucks.budget.state :as budget]
            [bucks.budget.core :as budget.core]
            [bucks.budget.components.budget-info :as budget-info]
            [bucks.budget.components.budget-input :as budget-input]
            [bucks.budget.components.clear-budget :as clear-budget]
            ["simple-statistics" :refer (mean quantile sum)]))


(defn- entry-month [e]
  (utils/date->month (:date e)))


(defn- all-entries [accounts]
  (->>  accounts
        (map #(vals (:entries %)))
        (reduce into)))


(defn- grouped-entries [entries]
  (let [{:keys [income expense refund transfer in out]} (group-by :type entries)]
    {:income income
     :expense (concat refund expense)
     :savings (concat in out)}))


(defn- entries-by-tag-by-month [available-tags entries]
  (->> entries
       (map (fn [e]
              (let [{:keys [tag color]} (budget.core/group-tags available-tags (:tags e))]
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


(defn- budget-tbody [group months entry-tag-months stats]
  (let [current-budget @(rf/subscribe [::budget/current-budget])
        input-enabled? @(rf/subscribe [::budget/input-enabled?])
        tags (->> entry-tag-months
                  (map :tag)
                  set
                  (sort-by first))
        entries (->> entry-tag-months
                     (group-by :tag)
                     (map (fn [[tag e]]
                            [tag (->> e
                                      (map (juxt :month identity))
                                      (into {}))]))
                     (into {}))]
    [:tbody
     (->> tags
          (map-indexed
           (fn [i tag]
             (let [[color label] tag
                   stats' (get stats tag)
                   label (if (empty? label) "un-tagged" label)
                   budget-item (get-in current-budget [group label] (budget.core/budget-item label group color))]
               [:tr {:key i}
                [:th {:style {:border-left (str "10px solid " color) :color color}}
                 label]
                [:th.has-text-right
                 [budget-input/input budget-item]]
                (->> stats'
                     (map-indexed
                      (fn [i [k v]]
                        [:td.has-text-right
                         {:key i
                          :style {:cursor "pointer"
                                  :background-color
                                  (when (= k (:stat budget-item)) "#72beff")}
                          :on-click #(budget/set-budget-item (budget.core/set-stat budget-item k (:amount-base v)))}
                         (:amount-base-p v)])))
                #_[:td.has-text-right (get-in stats [tag :avg :amount-base-p])]
                (->> months
                     (map-indexed
                      (fn [j month]
                        (let [{:keys [amount-base amount-base-p]} (get-in entries [tag month])]
                          [:td.has-text-right
                           {:key (str i "-" j)
                            :style {:cursor "pointer"}
                            :on-click #(budget/set-budget-item
                                        (budget.core/budget-item label group color
                                                                 :amount-base amount-base))}
                           amount-base-p]))))]))))]))


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
    [:th] [:th]

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
    [:th "budgeted"]
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
                    stats' {:sum (sum amounts)
                            :max (apply min-max amounts')
                            :mean mean
                            :q70 (quantile amounts q70)}]
                [tag (->> stats'
                          (map (fn [[k v]] [k (->amounts v)]))
                          (into {}))])))
           (into {})))))


(defn- controls []
  [:div.buttons
   [budget-input/toggle]
   [clear-budget/clear]])


(defn page []
  (let [budget-accounts @(rf/subscribe [::accounts/accounts-by-type :budget])
        bucket-accounts @(rf/subscribe [::accounts/accounts-by-type :bucket])
        available-tags @(rf/subscribe [::tags.state/available-tags-map])
        entries (-> (concat budget-accounts bucket-accounts) all-entries grouped-entries)

        income-tag-months (entries-by-tag-by-month available-tags (:income entries))
        expense-tag-months (entries-by-tag-by-month available-tags (:expense entries))
        savings-tag-months (entries-by-tag-by-month available-tags (:savings entries))

        months (->> (concat income-tag-months expense-tag-months savings-tag-months)
                    (map :month)
                    set
                    sort
                    reverse)
        income-stats (stats months income-tag-months)
        expense-stats (stats months expense-tag-months)
        savings-stats (stats months savings-tag-months)
        income-budget-totals (budget-totals months income-tag-months)
        expense-budget-totals (budget-totals months expense-tag-months)
        savings-budget-totals (budget-totals months savings-tag-months)
        combined-budget-totals (combined-totals income-budget-totals expense-budget-totals)]
    [:div
     #_[:pre (str savings-tag-months )]
     [budget-info/budget-info]
     [controls]
     [shared/table
      [months-thead "Total" income-stats months]
      [budget-totals-tbody income-stats combined-budget-totals]
      [space]
      [months-thead "Income" income-stats months]
      [budget-tbody :income months income-tag-months income-stats]
      [budget-totals-tbody income-stats income-budget-totals]
      [space]
      [months-thead "Savings" savings-stats months]
      [budget-tbody :savings months savings-tag-months savings-stats]
      [budget-totals-tbody savings-stats savings-budget-totals]
      [space]
      [months-thead "Expense" expense-stats months]
      [budget-tbody :expense months expense-tag-months expense-stats]
      [budget-totals-tbody income-stats expense-budget-totals]]
     [controls]]))
