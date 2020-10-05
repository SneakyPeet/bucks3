(ns bucks.pages.core
  (:require [re-frame.core :as rf]))


(rf/reg-event-db
 ::go-to-page
 (fn [db [_ page]]
   (let [history (::history db [])]
     (assoc db
            ::current-page page
            ::history (conj history (::current-page db))))))


(rf/reg-event-db
 ::go-to-previous-page
 (fn [db _]
   (let [history (::history db [])]
     (if (empty? history)
       (assoc db ::current-page :accounts)
       (-> db
           (assoc ::current-page (peek history))
           (update ::history pop))))))


(defn go-to-page [p]
  (rf/dispatch [::go-to-page p]))


(defn go-back []
  (rf/dispatch [::go-to-previous-page]))


(rf/reg-sub
 ::current-page
 (fn [db _]
   (::current-page db)))
