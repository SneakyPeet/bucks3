(ns bucks.app
  (:require [goog.events :as events]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [reagent.dom :as rd]
            [bucks.accounts.state :as accounts]
            [bucks.pages.registry :as pages]
            [bucks.options.state :as options]
            [bucks.tags.state :as tags]
            [akiroz.re-frame.storage :refer [reg-co-fx!]]
            ["file-saver" :as file-saver]
            ["moment" :as moment]
            [cljs.reader :refer [read-string]]))


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
            (accounts/init-state localstore)
            (tags/init-state localstore)
            (pages/init-state))}))


(rf/reg-event-fx
 ::clean
 [(rf/inject-cofx :localstore)]
 (fn [{:keys [localstore]} [_ k v]]
   {:localstore nil
    :dispatch [::initialize]}))


(rf/reg-event-fx
 ::save
 [(rf/inject-cofx :localstore)]
 (fn [{:keys [localstore]} _]
   (let [now (.format (moment) "YYYY-MM-DD-hh-mm")
         name (str now "-bucks.txt")
         content (pr-str (update localstore :options dissoc :fixer-api-key))
         blob (js/Blob. (array content) #js {:type "text/plain;charset=utf-8"})]
     (.saveAs file-saver blob name))
   {:localstore localstore}))


(rf/reg-event-fx
 ::restore
 [(rf/inject-cofx :localstore)]
 (fn [{:keys [localstore]} [_ data]]
   {:localstore (update data :options (fn [o] (merge (:options localstore) o)))
    :dispatch [::initialize]}))


(defn- read-files [files]
  (try
    (let [file (aget files 0)
          reader (js/FileReader.)]
      (set! (.-onload reader)
            (fn [e]
              (prn 1)
              (rf/dispatch [::restore (read-string (.. e -target -result))])))
      (.readAsText reader file))
    (catch :default e
      (js/alert e))))


(defn nav []
  [:nav.navbar
   [:div.navbar-menu
    [:div.navbar-end
     [:div.navbar-item
      [:div.buttons.mb-0 {:style {:align-items "flex-start"}}
       [:button.button.is-small
        {:on-click #(rf/dispatch [::save])}
        "save"]
       [:div.file.is-small
        [:label.file-label
         [:input.file-input {:type "file" :name "restore" :accept ".txt"
                             :on-change (fn [e]
                                          (read-files (.. e -target -files))
                                          (set! (.. e -target -value) ""))}]
         [:span.file-cta
          [:span.file-label "restore"]]]]]]
     ]]])

(defn app []
  [:div.section.pt-0
   [nav]
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
