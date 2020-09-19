(ns bucks.accounts.core
  (:require [re-frame.core :as rf]
            [bucks.pages.core :as pages]
            [bucks.options.core :as opts]))

(def account-config
  {:investment {:entry-types []
                :default (fn [entry] nil)}
   :budget {:entry-types [:expense :income :transfer :refund]
            :default (fn [entry] (if (pos? (:amount entry)) :income :expense))}})


(def account-types (keys account-config))


(defn new-account [id]
  {:id id
   :name ""
   :account-type :investment
   :date-format nil
   :header-types {}
   :header-index 0
   :current-balance 0
   :entries {}
   :currency (:base-currency opts/default-options)})


(defn sort-entries [entries]
  (->> entries
       (sort-by (juxt :date :import-index))))


(defn calculate-exchange-value [exchange-rate v]
  (Math/round (/ v exchange-rate)))


(defn re-balance [entries]
  (loop [current-balance 0
         exchange-balance 0
         result []
         entries (sort-entries entries)]
    (if (empty? entries)
      {:current-balance current-balance
       :current-balance-base exchange-balance
       :entries result}
      (let [{:keys [balance amount exchange-rate] :as entry} (first entries)
            entry-balance (if (= :not-provided balance)
                            (+ current-balance amount)
                            balance)
            entry (assoc entry :calculated-balance entry-balance)
            entry (if (= 1 exchange-rate)
                    (assoc entry
                           :calculated-balance-base entry-balance
                           :amount-base amount)
                    (assoc entry
                           :calculated-balance-base (calculate-exchange-value exchange-rate entry-balance)
                           :amount-base (calculate-exchange-value exchange-rate amount)))]
        (recur entry-balance
               (:calculated-balance-base entry)
               (conj result entry)
               (rest entries))))))


(defn entries->saveable [entries]
  (->> entries
       vals
       (map #(select-keys % [:amount :balance :import-id :id :note :tags :date :description :import-index
                             :exchange-rate :type]))
       (map (juxt :id identity))
       (into {})))
