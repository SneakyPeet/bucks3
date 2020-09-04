(ns bucks.app
  (:require [goog.events :as events]
            [reagent.core :as r]
            [reagent.dom :as rd]
            [bucks.import.core :as import]))


(def opts {:date-format "DD/MM/YYYY"
           :header-types {"Money in" :amount-in}})

(defn app []
  [:div.section
   [:div.container
    [import/component opts #(js/console.log %)]]])


(defn mount-reagent []
  (rd/render app (js/document.getElementById "app")))


(defn ^:export run []
  (mount-reagent))


(defn- listen []
  (events/listen js/window "load" #(run)))

(defonce listening? (listen))


(defn ^:dev/after-load shaddow-start [] (mount-reagent))
(defn ^:dev/before-load shaddow-stop [])
