(ns bucks.components.fixer-io
  (:require [cljs-bean.core :refer [->clj]]))


(defn fetch-rate [& {:keys [access-key date base-currency destination-currency success fail]}]
  (let [endpoint (str "http://data.fixer.io/api/"
                      date
                      "?access_key=" access-key
                      "&symbols=" base-currency "," destination-currency)]
    (-> (js/window.fetch endpoint)
        (.then #(.json %))
        (.then ->clj)
        (.then (fn [{success? :success rates :rates error :error}]
                 (if success?
                   (success
                    (/ (get rates (keyword destination-currency))
                       (get rates (keyword base-currency))))
                   (fail error)))))))
