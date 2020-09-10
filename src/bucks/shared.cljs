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


(defn table-cell-input [value & {:keys [placeholder on-change]
                                 :or {placeholder "I need a name"
                                      on-change prn}}]
  (let [change (fn [e] (on-change (.. e -target -value)))]
    [:input.input.is-small
     {:value value
      :placeholder placeholder
      :on-change change}]))