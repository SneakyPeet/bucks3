(ns bucks.budget.pages.budget
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [bucks.budget.state :as budget]
            [bucks.budget.core :as budget.core]
            [bucks.tags.state :as tags.state]
            [bucks.shared :as shared]
            [bucks.tags.components.tag-input :as tag-input]
            [bucks.budget.components.budget-input :as budget-input]
            [bucks.budget.components.clear-budget :as clear-budget]
            [bucks.charts :as charts]
            [bucks.utils :as utils]
            [clojure.string :as string]
            [cljs-bean.core :refer [->clj]]))


(rf/reg-event-db
 ::set-tags
 (fn [db [_ tags]]
   (assoc db ::tags tags)))


(rf/reg-sub
 ::tags
 (fn [db _]
   (::tags db [])))


(defn- add-budget-items []
  (let [tags @(rf/subscribe [::tags])
        available-tags @(rf/subscribe [::tags.state/available-tags-map])
        disabled? (empty? tags)
        add (fn [group]
              (let [{:keys [tag color]} (budget.core/group-tags available-tags tags)]
                (budget/set-budget-item
                   (budget.core/budget-item tag group color))))]
    [:div
     [:h1.heading "Add Budget Item"]
     [tag-input/tags tags
      :placeholder "tags"
      :on-change #(rf/dispatch [::set-tags %])]
     [:div.buttons.mt-3
      [:button.button.is-success.is-small
       {:disabled disabled?
        :on-click #(add :savings)}
       "Add Saving"]
      [:button.button.is-danger.is-small
       {:disabled disabled?
        :on-click #(add :expense)}
       "Add Expense"]
      [budget-input/toggle]
      [clear-budget/clear]]]))



(defn budget-table []
  (let [{:keys [savings expense]} @(rf/subscribe [::budget/current-budget])
        income @(rf/subscribe [::budget/income-budgeted])
        expense (->> (vals savings)
                     (concat (vals expense))
                     (map #(assoc % :percentage (utils/percentage (:amount-base %) income)))
                     (sort-by (juxt :color :amount-base)))
        {:keys [total total-p]} (->> expense
                                     (reduce
                                      (fn [r {:keys [amount-base percentage]}]
                                        (-> r
                                            (update :total + amount-base)
                                            (update :total-p + percentage)))
                                      {:total 0
                                       :total-p 0}))
        available (- income total)]
    [shared/table
     [:tbody
      (->> expense
           (map-indexed
            (fn [i {:keys [id color group amount-base percentage] :as item}]
              [:tr {:key i}
               [:th {:style {:color color}} id
                (when (= :savings group) [:small.has-text-grey-light " (saving)"])]
               [:td.has-text-right
                [budget-input/input item]]
               [:td.has-text-right percentage "%"]
               [:td [:a.has-text-danger
                     {:on-click
                      (fn []
                        (when (js/confirm "Remove?")
                          (budget/remove-budget-item (dissoc item :percentage))))}
                     "remove"]]])))
      [:tr
       [:th "total"]
       [:th.has-text-right (utils/format-cents total)]
       [:th.has-text-right (utils/round total-p) "%"]]
      [:tr
       [:th.has-text-success "available"]
       [:th.has-text-right
        {:class (if (neg? available) "has-text-danger" "has-text-success")}
        (utils/format-cents available)]]]]))

(defn- tooltip [d]
  (let [items (->> (:payload (->clj d))
                   first
                   :payload
                   :items)]
    (->> items
         (map #(str (:id %) ": " (utils/format-cents (:amount-base %))))
         (string/join "\n")))
  )

(defn- bar []
  (let [{:keys [savings income expense]} @(rf/subscribe [::budget/current-budget])
        income @(rf/subscribe [::budget/income-budgeted])
        income (/ income 100)

        values (->> (vals expense)
                    (concat (vals savings))
                    (group-by :color)
                    (map (fn [[color e]]
                           {:id (keyword (str "_" (string/replace color #"#" "")))
                            :color color
                            :fill color
                            :items (->> e
                                        (map #(select-keys % [:id :amount-base])))
                            :total (/ (->> e
                                           (map :amount-base)
                                           (reduce +)
                                           Math/abs) 100)}))
                    (filter #(not (zero? (:total %))))
                    (sort-by :color)
                    reverse)
        data (->> values
                  (map (juxt :id :total))
                  (into {}))
        expense-bar (assoc data :name "Budget")
        income-bar {:name "Income" :income income}]
    [:div
     [:div
      [charts/bar-chart {:width 200 :height 400
                         :data (clj->js [income-bar expense-bar])}
       [charts/x-axis {:dataKey "name"}]
       [charts/y-axis]
       [charts/reference-line {:y income :stroke "red" :strokeDasharray "3 3"
                               :isFront true :strokeWidth 2}]
       [charts/bar {:id :income :dataKey :income :stackId "a" :fill "green"}]
       (->> values
            (map-indexed
             (fn [i {:keys [id color]}]
               [charts/bar {:key i :id id :dataKey id :stackId "a" :fill color}])))]]
     [:div
      [charts/responsive-container {:width "100%" :height 200}
       [charts/bar-chart {:data (clj->js values)}
        [charts/y-axis]
        [charts/cartesian-grid {:strokeDasharray "3 3"}]
        [charts/tooltip {:content tooltip}]
        [charts/bar {:dataKey :total}]]]]]))


(defn page []
  [:div.columns
   [:div.column.is-narrow
    [budget-table]
    [:hr]
    [add-budget-items]]
   [:div.column
    [bar]
    #_[charts/bar-chart {:width 730 :height 200 :data (clj->js [{:name "test" :uv 123 :pv 234}])}
     [charts/bar {:dataKey "uv" :fill "#efefef"}]
     [charts/bar {:dataKey "pv" :fill "#efefef"}]]]])
