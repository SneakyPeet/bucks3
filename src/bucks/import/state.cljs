(ns bucks.import.state
  (:require [re-frame.core :as rf]
            [bucks.import.core :as import]
            [bucks.accounts.state :as accounts]))


(rf/reg-event-db
 ::import-entries
 (fn [db [_ id new-entries]]
   (assoc db ::imported-entries
          (import/process-entries
           (vals (get-in db [::accounts/accounts id :entries]))
           new-entries))))


(defn import-entries [account-id entries]
  (rf/dispatch [::import-entries account-id entries]))


(rf/reg-sub
 ::imported-entries
 (fn [db _]
   (::imported-entries db)))


(rf/reg-sub
 ::exchange-rates
 (fn [db [_ source destination]]
   (get-in db [::exchange-rates source destination] {})))


(rf/reg-event-db
 ::set-exchange-rate
 (fn [db [_ source destination date rate]]
   (assoc-in db [::exchange-rates source destination date] rate)))


(defn set-exchange-rate [source destination date rate]
  (rf/dispatch [::set-exchange-rate source destination date rate]))


(rf/reg-sub
 ::exchange-rate-error
 (fn [db _]
   (get db ::exchange-rate-error)))


(rf/reg-event-db
 ::set-exchange-rate-error
 (fn [db [_ err]]
   (assoc db ::exchange-rate-error err)))


(defn set-exchange-rate-error [err]
  (rf/dispatch [::set-exchange-rate-error err]))

(defn clear-exchange-rate-error []
  (set-exchange-rate-error nil))


(rf/reg-event-db
 ::finish-import
 (fn [db _]
   (dissoc db ::imported-entries)))


(defn finish-import []
  (rf/dispatch [::finish-import]))


(defn confirm-entries [account-id entries]
  (let [entries (->> entries
                     (filter (fn [e]
                               (and (not (:existing? e))
                                    (not (:duplicate? e))))))]
    (when-not (empty? entries)
      (accounts/add-entries account-id entries))))
