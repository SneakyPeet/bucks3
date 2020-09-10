(ns bucks.tags.core)


(defn new-tag [label]
  {:id (str (random-uuid))
   :label label
   :color "#e5e5e5"})
