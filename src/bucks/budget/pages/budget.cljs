(ns bucks.budget.pages.budget
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [bucks.budget.state :as budget]
            [bucks.budget.core :as budget.core]
            [bucks.tags.state :as tags.state]
            [bucks.shared :as shared]
            [bucks.tags.components.tag-input :as tag-input]
            [bucks.utils :as utils]))


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
       "Add Expense"]]]))


(defn budget-table []
  (let [{:keys [savings income expense]} @(rf/subscribe [::budget/current-budget])
        income (->> (vals income)
                    (map :amount-base)
                    (reduce +))
        expense (->> (vals savings)
                     (concat (vals expense))
                     (map (fn [e]
                            (let [ab (- (Math/abs (:amount-base e)))]
                              (-> e
                                  (assoc :amount-base ab
                                         :amount-base-p (utils/format-cents ab)
                                         :precentage (Math/abs (/ (Math/round (* 10000 (/ ab income))) 100)))))))
                     (sort-by (juxt :color :amount-base )))
        {:keys [total total-p]} (->> expense
                                     (reduce
                                      (fn [r {:keys [amount-base precentage]}]
                                        (-> r
                                            (update :total + amount-base)
                                            (update :total-p + precentage)))
                                      {:total 0
                                       :total-p 0}))
        available (+ income total)]
    [shared/table
     [:tbody
      (->> expense
           (map-indexed
            (fn [i {:keys [id amount-base-p color group precentage] :as item}]
              [:tr {:key i}
               [:th {:style {:color color}} id
                (when (= :savings group) [:small.has-text-grey-light " (saving)"])]
               [:td.has-text-right amount-base-p]
               [:td.has-text-right precentage "%"]
               [:td [:a.has-text-danger
                     {:on-click
                      (fn []
                        (when (js/confirm "Remove?")
                          (budget/remove-budget-item item)))}
                     "remove"]]])))
      [:tr
       [:th.has-text-danger "total"]
       [:th.has-text-right (utils/format-cents total)]
       [:th.has-text-right total-p "%"]]
      [:tr
       [:th.has-text-success "available"]
       [:th.has-text-right
        {:class (if (neg? available) "has-text-danger" "has-text-success")}
        (utils/format-cents available)]]]]))



(defn page []
  [:div
   [budget-table]
   [:hr]
   [add-budget-items]])
