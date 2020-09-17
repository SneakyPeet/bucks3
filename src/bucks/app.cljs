(ns bucks.app
  (:require [goog.events :as events]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [reagent.dom :as rd]
            [bucks.accounts.state :as accounts]
            [bucks.pages.registry :as pages]
            [bucks.options.state :as options]
            [akiroz.re-frame.storage :refer [reg-co-fx!]]))


(reg-co-fx! :bucks3
            {:fx :localstore
             :cofx :localstore})


(rf/reg-event-fx
 ::initialize
 [(rf/inject-cofx :localstore)]
 (fn [{:keys [localstore]} _]
   {:localstore localstore
    :db (-> {}
            (options/init-state localstore)
            (accounts/init-state)
            (pages/init-state))}))



(defn app []
  [:div.section
   [:div.container
    [pages/component]]])


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
