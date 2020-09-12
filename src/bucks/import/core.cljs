(ns bucks.import.core
  (:require [re-frame.core :as rf]
            [bucks.accounts.core :as a]))


(defn- prep-new [import-id entry]
  (let [amount (if-let [amount (:amount entry)]
                 amount
                 (- (:amount-in entry 0) (:amount-out entry 0)))
        balance (if-let [balance (:balance entry)]
                  balance
                  :not-provided)]
    (-> entry
        (dissoc :amount-in :amount-out)
        (assoc :amount amount
               :balance balance
               :import-id import-id
               :id (str (random-uuid))
               :note ""
               :tags #{}))))

(defn- comparable [e]
  (select-keys e [:date :amount :balance :description]))


(defn process-entries [existing-entries new-entries]
  (let [import-id (.getTime (js/Date.))
        lookup (->> existing-entries
                    (map comparable)
                    set)
        new-entries (->> new-entries
                         (map (fn [e]
                                (let [prepped (prep-new import-id e)
                                      compare (comparable prepped)]
                                  (if (contains? lookup compare)
                                    (assoc prepped :duplicate? true)
                                    prepped))))
                         (sort-by :date))
        oldest (:date (first new-entries))
        newest (:date (last new-entries))
          existing (->> existing-entries
                      (filter (fn [{:keys [date]}]
                                (and (>= date oldest)
                                     (<= date newest))))
                      (map #(assoc % :existing? true)))]
    (->> new-entries
         (into existing)
         (sort-by (juxt :date :description :amount))
         reverse)))
