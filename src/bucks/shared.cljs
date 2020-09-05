(ns bucks.shared)


(defn heading [& s]
  [:div
   [:h1.heading
    (map-indexed (fn [i v] [:<> {:key i} v]) s)]
   [:hr]])