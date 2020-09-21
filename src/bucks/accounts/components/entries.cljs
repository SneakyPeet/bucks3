(ns bucks.accounts.components.entries
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [bucks.accounts.state :as accounts]
            [bucks.accounts.core :as accounts.core]
            [bucks.shared :as shared]
            [bucks.pages.core :as pages]
            [bucks.tags.components.tag-input :as tag-input]
            [bucks.tags.state :as tags.state]
            [clojure.string :as string]))


(def entry-type-options
  (->> accounts.core/account-config
       (map (fn [[k v]]
              [k (map (fn [t] {:label (name t) :value t}) (:entry-types v))]))
       (into {})))


(defn filter-form [*filter]
  (r/with-let [*local (r/atom "")]
    [:div.field.has-addons
     [:div.control
      [:input.input {:type "text"
                     :placeholder "search description"
                     :value @*local
                     :on-change #(reset! *local (.. % -target -value))}]]
     [:div.control
      [:a.button.is-info
       {:on-click #(reset! *filter (string/lower-case @*local))}
       "Filter"]]
     [:div.control
      [:a.button
       {:on-click #(do (reset! *filter "")
                       (reset! *local ""))}
       "Clear"]]]))


(defn- update-entry-tags [f account-id tags tag-label entries]
  (let [tags' (->> tags
                   (map (juxt :label :id))
                   (into {}))
        tag-id (get tags' tag-label)]
    (when tag-id
      (doseq [e entries]
        (accounts/update-entry-tags account-id (:id e) (f (:tags e) tag-id))))))


(defn- add-tag-to-all [account-id entries]
  (r/with-let [tags @(rf/subscribe [::tags.state/available-tags])
               tag-map (->> tags
                            (map (juxt :label :id))
                            (into {}))
               *tag (r/atom (:label (first tags)))]
    (when-not (empty? tags)
      [:div.field.has-addons
       [:div.control
        [:div.select
         [:select {:on-change #(reset! *tag (.. % -target -value))}
          (->> tags
               (sort-by :label)
               (map-indexed
                (fn [i {:keys [label]}]
                  [:option {:key i} label])))]]]
       [:div.control
        [:a.button.is-info
         {:on-click #(update-entry-tags conj account-id tags @*tag entries)
          :disabled (empty? @*tag)}
         "Add"]]
       [:div.control
        [:a.button.is-danger
         {:on-click #(update-entry-tags disj account-id tags @*tag entries)
          :disabled (empty? @*tag)}
         "Remove"]]])))


(defn component [account-id entries]
  (r/with-let
    [*filter (r/atom "")]
    (let [account-type (:account-type @(rf/subscribe [::accounts/account-data account-id]))
          entry-types (get entry-type-options account-type)
          filt @*filter
          entries (if (empty? filt)
                    entries
                    (->> entries
                         (filter (fn [e]
                                   (string/includes? (string/lower-case (:description e)) filt)))))]
      [:div
       [:div.columns
        [:div.column [filter-form *filter]]
        [:div.column [add-tag-to-all account-id entries]]]
       [:div.table-container.is-size-7
        [:table.table.is-striped
         [:thead
          [:tr [:th "Date"] [:th "Description"]
           [:th "Amount"] [:th "Balance"] [:th "Exchange Rate"]
           [:th "Type"]
           [:th "Note"] [:th "Tags"]]]
         [:tbody
          (->> entries
               (map-indexed
                (fn [i {:keys [id date description amount amount-p
                               calculated-balance-p exchange-rate note tags] :as entry}]
                  [:tr {:key i}
                   [:td {:class (when-not (neg? amount) "is-selected")} date]
                   [:td description]

                   [:td.has-text-right amount-p]
                   [:td calculated-balance-p]
                   [:td.has-text-right exchange-rate]

                   [:td [shared/select entry-types (:type entry)
                         :on-change #(accounts/update-entry-type
                                      account-id id %)]]
                   [:td [shared/table-cell-input note
                         :placeholder "note"
                         :on-change  #(accounts/update-entry-note
                                       account-id id %)]]
                   [:td [tag-input/tags tags
                         :placeholder "tags"
                         :on-change #(accounts/update-entry-tags
                                      account-id id %)]]])))]]]])))
