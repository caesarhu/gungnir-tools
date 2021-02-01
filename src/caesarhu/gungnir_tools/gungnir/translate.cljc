(ns caesarhu.gungnir-tools.gungnir.translate
  (:require
    [clojure.set]
    [malli.core :as m]
    [medley.core :as medley]
    [gungnir.model :as gm]
    [gungnir.util.malli :refer [child-properties]]))

(def translate-key :locale/zh-tw)

(defn model->dict
  [model]
  (let [table-map {(gm/table model) (some-> model m/properties translate-key)}
        locale-fn (fn [child] (some-> child child-properties translate-key))
        half-map (->> model
                      (m/children)
                      (map #(hash-map (first %) (locale-fn %)))
                      (apply merge)
                      (merge table-map)
                      (medley/filter-vals some?))]
    (merge half-map (clojure.set/map-invert half-map))))

(defn translate
  [dict k]
  (or (get dict k)
      k))
