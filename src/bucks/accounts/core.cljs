(ns bucks.accounts.core
  (:require [re-frame.core :as rf]
            [bucks.pages.core :as pages]))


(defn new-account [id]
  {:id id
   :name ""
   :date-format nil
   :header-types {}
   :header-index 0
   :current-balance 0
   :entries {}})


(defn sort-entries [entries]
  (->> entries
       (sort-by (juxt :date :import-index))))


(defn re-balance [entries]
  (loop [current-balance 0
         result []
         entries (sort-entries entries)]
    (if (empty? entries)
      {:current-balance current-balance
       :entries result}
      (let [{:keys [balance amount] :as entry} (first entries)
            entry-balance (if (= :not-provided balance)
                            (+ current-balance amount)
                            balance)
            entry (assoc entry :calculated-balance entry-balance)]

        (recur entry-balance
               (conj result entry)
               (rest entries))))))
