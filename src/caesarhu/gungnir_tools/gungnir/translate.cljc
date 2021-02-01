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
  (let [table-locale (some-> model m/properties translate-key)
        table-map {(gm/table model) (keyword table-locale)}
        locale-fn (fn [child]
                    (if-let [child-locale (some-> child child-properties translate-key)]
                      (keyword table-locale child-locale)))
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
