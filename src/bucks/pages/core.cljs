(ns bucks.pages.core
  (:require [re-frame.core :as rf]))


(rf/reg-event-db
 ::go-to-page
 (fn [db [_ page]]
   (assoc db ::current-page page)))


(defn go-to-page [p]
  (rf/dispatch [::go-to-page p]))

(rf/reg-sub
 ::current-page
 (fn [db _]
   (::current-page db)))
