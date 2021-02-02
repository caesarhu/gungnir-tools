(ns caesarhu.gungnir-tools.gungnir.translate
  (:require
    [clojure.set]
    [malli.core :as m]
    [medley.core :as medley]
    [gungnir.model :as gm]
    [gungnir.util.malli :refer [child-properties]]
    [caesarhu.gungnir-tools.config :refer [translate-key*]]))

(defn model->dict
  [model]
  (let [table-locale (@translate-key* (some-> model m/properties))
        table-map {(gm/table model) (keyword table-locale)}
        locale-fn (fn [child]
                    (if-let [child-locale (@translate-key* (some-> child child-properties))]
                      (keyword table-locale child-locale)))
        half-map (->> model
                      (m/children)
                      (map #(hash-map (first %) (locale-fn %)))
                      (apply merge)
                      (merge table-map)
                      (medley/filter-vals some?))]
    (merge half-map (clojure.set/map-invert half-map))))

(defn models->dict
  ([models]
   (->> models
        vals
        (map model->dict)
        (apply merge)))
  ([]
   (models->dict @gm/models)))

(defn translate
  [dict k]
  (or (get dict k)
      k))
