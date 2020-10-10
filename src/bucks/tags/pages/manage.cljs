(ns bucks.tags.pages.manage
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [bucks.tags.state :as tag.state]
            [bucks.accounts.state :as accounts]
            [bucks.shared :as shared]
            ["react-color" :refer (CompactPicker)]
            [bucks.pages.core :as pages]))

(def ^:private react-color (r/adapt-react-class CompactPicker))

(defn- color-picker []
  (r/with-let [*color (r/atom "#000")]
    [:div.px-1.py-1.mb-3 {:style {:background-color @*color :width "fit-content" :border-radius "0.5rem"}}
     [react-color {:color @*color
                   :onChangeComplete #(reset! *color (.-hex %))}]]))


(defn- color-box [color]
  [:div.ml-1.mr-1
   {:style {:display "inline-block"
            :width "1.5rem" :height "1.5rem"
            :background-color color}}])


(defn- tag-usage [accounts]
  (->> accounts
       (map #(-> % :entries vals))
       (reduce into)
       (map #(-> % :tags vec))
       (reduce into)
       frequencies))


(defn- tags-list []
  (let [all-tags @(rf/subscribe [::tag.state/available-tags])
        accounts @(rf/subscribe [::accounts/accounts])
        tag-usage (tag-usage accounts)
        groups @(rf/subscribe [::tag.state/tag-group-color-names])]
    (when-not (empty? all-tags)
      [shared/table
       [:thead
        [:tr  [:th "tag"] [:th "color"] [:th "group"] [:th] [:th "usage"] [:th]]]
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
                 [:td (get groups color "")]
                 [:td [color-box color]]
                 [:td (if-let [c (get tag-usage id)]
                        c
                        [:a.has-text-danger {:on-click #(tag.state/remove-tag id)} "remove"])]
                 [:td [:a {:on-click #(do
                                        (tag.state/select-tag id)
                                        (pages/go-to-page :tag-entries))}
                       "view"]]])))]])))


(defn- groups []
  (let [tag-groups @(rf/subscribe [::tag.state/tag-groups])]
    (when-not (empty? tag-groups)
      [shared/table
       [:thead
        [:tr [:th {:colSpan 2} "Tag Groups"]]
        [:tr  [:th "Name"] [:th "Color"] [:th]]]
       [:tbody
        (->> tag-groups
             (map-indexed
              (fn [i {:keys [name color] :as group}]
                [:tr {:key i}
                 [:td [shared/table-cell-input
                       name
                       :on-change #(tag.state/edit-tag-group-name group %)]]
                 [:td [shared/table-cell-input
                       color
                       :on-change #(tag.state/edit-tag-group-color group %)]]
                 [:td [color-box color]]
                 #_[:td (if-let [c (get tag-usage id)]
                        c
                        [:a.has-text-danger {:on-click #(tag.state/remove-tag id)} "remove"])]
                 #_[:td [:a {:on-click #(do
                                        (tag.state/select-tag id)
                                        (pages/go-to-page :tag-entries))}
                       "view"]]])))]]
      )))


(defn page []
  (let [all-tags @(rf/subscribe [::tag.state/available-tags])
        accounts @(rf/subscribe [::accounts/accounts])
        tag-usage (tag-usage accounts)]
    [:div
     [:button.button.is-small.is-primary
      {:on-click #(tag.state/add-tag)}
      "Add Tag"]
     [:div.columns
      [:div.column
       [tags-list]]
      [:div.column
       [color-picker]
       [:button.button.is-small.is-primary
        {:on-click #(tag.state/add-tag-group)}
        "Add Group"]
       [groups]]]]))
