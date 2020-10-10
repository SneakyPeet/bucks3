(ns bucks.tags.core)


(defn new-tag [label]
  {:id (str (random-uuid))
   :label label
   :color "#AFAFAF"})


(defn new-tag-group
  [color name]
  {:id (str (random-uuid))
   :color color
   :name name})
