(ns bucks.pages.core
  (:require [re-frame.core :as rf]))


(rf/reg-event-db
 ::go-to-page
 (fn [db [_ page]]
   (assoc db
          ::current-page page
          ::previous-page (::current-page db))))


(rf/reg-event-db
 ::go-to-previous-page
 (fn [db _]
   (when-let [p (::previous-page db)]
     (-> db
         (assoc ::current-page p)
         (dissoc ::previous-page)))))


(defn go-to-page [p]
  (rf/dispatch [::go-to-page p]))


(defn go-back []
  (rf/dispatch [::go-to-previous-page]))


(rf/reg-sub
 ::current-page
 (fn [db _]
   (::current-page db)))
