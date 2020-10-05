(ns bucks.utils)


(def ^:private numformat (js/Intl.NumberFormat.))
(defn format-cents [c]
  (.format numformat (/ c 100)))


(defn date->month [d]
  (subs d 0 7))


(defn round [n]
  (/ (Math/round (* n 100)) 100))


(defn percentage [n f]
  (round (* 100 (/ n f))))
