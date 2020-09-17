(ns bucks.options.state
  (:require [bucks.options.core :as opts]
            [re-frame.core :as rf]))


(defn init-state [db localstore]
  (assoc db ::options
         (merge opts/default-options
                (:options localstore))))


(rf/reg-event-fx
 ::set-option
 [(rf/inject-cofx :localstore)]
 (fn [{:keys [db localstore]} [_ k v]]
   {:localstore (assoc-in localstore [:options k] v)
    :db (assoc-in db [::options k] v)}))


(defn set-option [k v]
  (rf/dispatch [::set-option k v]))


(rf/reg-sub
 ::options
 (fn [db _]
   (get db ::options)))
