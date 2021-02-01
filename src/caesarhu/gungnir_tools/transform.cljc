(ns caesarhu.gungnir-tools.transform
  (:require
    [malli.core :as m]
    [malli.util :as mu]
    [malli.transform :as mt]
    [caesarhu.gungnir-tools.gungnir.translate :refer [model->dict]]))

(defn union-transformer
  ([f]
   (let [transform {:compile (fn [schema _]
                                 (fn [x]
                                   (let [union-schema (mu/union schema x)]
                                     (if (fn? f)
                                       (f union-schema)
                                       union-schema))))}]
     (mt/transformer
       {:decoders {:map transform}
        :encoders {:map transform}})))
  ([]
   (union-transformer nil)))

(defn dict-transformer
  []
  #(union-transformer model->dict))