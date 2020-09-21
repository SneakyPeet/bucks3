(ns bucks.utils)


(def ^:private numformat (js/Intl.NumberFormat.))
(defn format-cents [c]
  (.format numformat (/ c 100)))
