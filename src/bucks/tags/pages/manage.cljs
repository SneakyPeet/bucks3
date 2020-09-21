(ns bucks.tags.pages.manage
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [bucks.tags.state :as tag.state]
            [bucks.shared :as shared]
            ["react-color" :refer (TwitterPicker)]))

(def ^:private react-color (r/adapt-react-class TwitterPicker))

(defn- color-picker []
  (r/with-let [*color (r/atom "#000")]
    [:div.px-1.py-1 {:style {:background-color @*color :width "fit-content" :border-radius "0.5rem"}}
     [react-color {:color @*color
                   :onChangeComplete #(reset! *color (.-hex %))
                   :triangle "hide"}]]))


(defn- color-box [color]
  [:div.ml-1.mr-1
   {:style {:display "inline-block"
            :width "1.5rem" :height "1.5rem"
            :background-color color}}])


(defn page []
  (let [all-tags @(rf/subscribe [::tag.state/available-tags])]
    [:div
     [:button.button.is-small.is-primary
      {:on-click #(tag.state/add-tag)}
      "Add Tag"]
    #_ [:pre (str all-tags)]
     (when-not (empty? all-tags)
       [:div.columns
        [:div.column
         [shared/table
          [:thead
           [:tr  [:th "tag"] [:th "color"] [:th]]]
          [:tbody
           (->> all-tags
                (map-indexed
                 (fn [i {:keys [id label color]}]
                   [:tr {:key i}
                    [:td [shared/table-cell-input
                          label
                          :on-change #(tag.state/update-label id %)]]
                    [:td [shared/table-cell-input
                          color
                          :placeholder "I need a color"
                          :on-change #(tag.state/update-color id %)]]
                    [:td [color-box color]]])))]]]
        [:div.column
         [color-picker]]])]))
