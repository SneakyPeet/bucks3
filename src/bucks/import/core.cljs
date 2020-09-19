(ns bucks.import.core
  (:require [re-frame.core :as rf]
            [bucks.accounts.core :as a]))


(defn import-id->str [i]
  (.toLocaleString (js/Date. i)))

(defn- prep-new [account-type import-id entry]
  (let [amount  (if-let [amount (:amount entry)]
                 amount
                 (- (:amount-in entry 0) (:amount-out entry 0)))
        balance (if-let [balance (:balance entry)]
                  balance
                  :not-provided)
        entry   (-> entry
                  (dissoc :amount-in :amount-out)
                  (assoc :amount amount
                         :balance balance
                         :import-id import-id
                         :id (str (random-uuid))
                         :note (:note entry "")
                         :tags #{}))
        entry-type ((get-in a/account-config [account-type :default]) entry)]
    (assoc entry :type entry-type)))


(defn- comparable [e]
  (select-keys e [:date :amount :balance :description]))


(defn process-entries [account-type existing-entries new-entries]
  (let [import-id (.getTime (js/Date.))
        lookup (->> existing-entries
                    (map comparable)
                    set)
        new-entries (->> new-entries
                         (map (fn [e]
                                (let [prepped (prep-new account-type import-id e)
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
         (sort-by (juxt :date :import-index))
         reverse)))
