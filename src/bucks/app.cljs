(ns bucks.app
  (:require [goog.events :as events]
            [reagent.core :as r]
            [reagent.dom :as rd]
            [bucks.import.core :as import]))


(defn app []
  [import/component])


(defn mount-reagent []
  (rd/render [app] (js/document.getElementById "app")))


(defn ^:export run []
  (mount-reagent))


(defn- listen []
  (events/listen js/window "load" #(run)))

(defonce listening? (listen))


(defn ^:dev/after-load shaddow-start [] (mount-reagent))
(defn ^:dev/before-load shaddow-stop [])
