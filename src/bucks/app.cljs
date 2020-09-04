(ns bucks.app
  (:require [goog.events :as events]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [reagent.dom :as rd]
            [bucks.import.core :as import]
            [bucks.accounts.core :as accounts]))


(rf/reg-event-db
 ::initialize
 (fn [_ _]
   (-> {}
       (accounts/init-state))))


(def opts {:date-format "DD/MM/YYYY"
           :header-types {"Money in" :amount-in}})


(defn app []
  [:div.section
   [:div.container
    [accounts/component]
    #_[import/component opts #(js/console.log %)]]])


(defn mount-reagent []
  (rd/render app (js/document.getElementById "app")))


(defn ^:export run []
  (rf/dispatch-sync [::initialize])
  (mount-reagent))


(defn- listen []
  (events/listen js/window "load" #(run)))

(defonce listening? (listen))


(defn ^:dev/after-load shaddow-start [] (mount-reagent))
(defn ^:dev/before-load shaddow-stop [])
