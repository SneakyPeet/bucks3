(ns bucks.budget.state
  (:require [re-frame.core :as rf]))


(defn init-state [db localstore]
  (assoc db ::current-budget (:current-budget localstore {})))


(rf/reg-event-fx
 ::set-budget-item
 [(rf/inject-cofx :localstore)]
 (fn [{:keys [db localstore]} [_ item]]
   {:db (assoc-in db [::current-budget (:group item) (:id item)] item)
    :localstore (assoc-in localstore [:current-budget (:group item) (:id item)] item)}))


(defn set-budget-item [item]
  (rf/dispatch [::set-budget-item item]))


(rf/reg-event-fx
 ::clear-current-budget
 [(rf/inject-cofx :localstore)]
 (fn [{:keys [db localstore]} [_ item]]
   {:db (assoc db ::current-budget {})
    :localstore (assoc localstore :current-budget {})}))


(defn clear-current-budget []
  (rf/dispatch [::clear-current-budget]))


(rf/reg-sub
 ::current-budget
 (fn [db _]
   (::current-budget db {})))
