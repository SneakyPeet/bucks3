(ns bucks.import.core
  (:require [reagent.core :as r]
            [cljs-bean.core :refer [->js ->clj]]
            ["papaparse" :refer (parse)]))


(defn- upload-button [read-file]
  [:form.file {:id "upload-form"}
   [:label.file-label
    [:input.file-input
     {:type "file"
      :name "upload"
      :on-change read-file}]
    [:span.file-cta
     [:span.file-icon
      [:i.fas.fa-upload]]
     [:span.file-label "Choose a file"]]]])


(defn- event->file [e]
  (aget (.. e -target -files) 0))


(defn- parse' [file f]
  (parse file
         (->js {:complete f})))


(defn- process-result [result]
  (->clj result))


(defn component []
  (r/with-let
    [*result (r/atom [])
     read-file (fn [e]
                 (parse' (event->file e)
                         #(reset! *result (process-result %))
                         ))]
    (fn []
      [:div
       [upload-button read-file]
       [:div [:pre (str @*result)]]])))
