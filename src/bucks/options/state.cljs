(ns bucks.options.state
  (:require [bucks.options.core :as opts]
            [re-frame.core :as rf]))


(defn init-state [db]
  (assoc db ::options opts/default-options))


(rf/reg-event-db
 ::set-option
 (fn [db [_ k v]]
   (assoc-in db [::options k] v)))


(defn set-option [k v]
  (rf/dispatch [::set-option k v]))


(rf/reg-sub
 ::options
 (fn [db _]
   (get db ::options)))
