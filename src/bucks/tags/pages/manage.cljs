(ns bucks.tags.pages.manage
  (:require [re-frame.core :as rf]
            [bucks.tags.state :as tag.state]
            [bucks.shared :as shared]))


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
     (when-not (empty? all-tags)
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
                  [:td [color-box color]]])))]])]))
