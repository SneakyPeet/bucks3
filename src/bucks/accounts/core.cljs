(ns bucks.accounts.core
  (:require [re-frame.core :as rf]
            [bucks.pages.core :as pages]))


(defn new-account [id]
  {:id id
   :name ""
   :date-format nil
   :header-types {}
   :header-index 0
   :entries {}})


(defn sort-entries [entries]
  (->> entries
       (sort-by :date)
       reverse))
