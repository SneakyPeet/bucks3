(ns bucks.shared)


(defn heading [& s]
  [:div
   [:h1.heading
    (map-indexed (fn [i v] [:<> {:key i} v]) s)]
   [:hr]])


(defn back [t f]
  [:small.is-pulled-right [:a {:on-click f} t]])


(defn table [& children]
  [:div.table-container.is-size-7
   [:table.table.is-striped
    (map-indexed
     (fn [i c] [:<> {:key i} c])
     children)]])
