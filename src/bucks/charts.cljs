(ns bucks.charts
  (:require [reagent.core :as r]
            ["recharts" :refer (ResponsiveContainer
                                ComposedChart
                                BarChart Bar
                                LineChart Line
                                AreaChart Area
                                ReferenceLine CartesianGrid
                                Legend XAxis YAxis Tooltip Brush LabelList)]))

(def responsive-container (r/adapt-react-class ResponsiveContainer))
(def composed-chart (r/adapt-react-class ComposedChart))

(def bar-chart (r/adapt-react-class BarChart))
(def bar (r/adapt-react-class Bar))

(def line-chart (r/adapt-react-class LineChart))
(def line (r/adapt-react-class Line))

(def area-chart (r/adapt-react-class AreaChart))
(def area (r/adapt-react-class Area))


(def reference-line (r/adapt-react-class ReferenceLine))

(def legend (r/adapt-react-class Legend))
(def x-axis (r/adapt-react-class XAxis))
(def y-axis (r/adapt-react-class YAxis))
(def cartesian-grid (r/adapt-react-class CartesianGrid))
(def tooltip (r/adapt-react-class Tooltip))
(def brush (r/adapt-react-class Brush))
(def label-list (r/adapt-react-class LabelList))
