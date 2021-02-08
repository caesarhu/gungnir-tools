(ns caesarhu.gungnir-tools.transform
  (:require
    [clojure.spec.alpha :as s]
    [malli.core :as m]
    [malli.util :as mu]
    [malli.transform :as mt]
    [caesarhu.gungnir-tools.gungnir.translate :refer [model->dict]]
    [caesarhu.gungnir-tools.lacinia :refer [model->object models->objects]]
    [caesarhu.gungnir-tools.postgres.table :refer [generate-table-edn models-table-edn]]))

(s/fdef union-transformer
  :args (s/alt :arity-2 (s/cat :f fn?
                               :m map?)
               :arity-1 (s/cat :f-or-m (s/or :function fn?
                                             :map map?))
               :arity-0 (s/cat)))
(defn union-transformer
  ([f m]
   (let [transform {:compile (fn [schema _]
                               (println schema)
                               (fn [x]
                                 (let [union-schema (mu/union schema x)
                                       result (if (fn? f)
                                                (f union-schema)
                                                union-schema)]
                                   (if (map? m)
                                     (merge result m)
                                     result))))}]
     (mt/transformer
       {:decoders {:map transform}
        :encoders {:map transform}})))
  ([f-or-m]
   (cond
     (fn? f-or-m) (union-transformer f-or-m nil)
     (map? f-or-m) (union-transformer nil f-or-m)
     :else (union-transformer nil nil)))
  ([]
   (union-transformer nil nil)))

(defn dict-transformer
  ([m]
   #(union-transformer model->dict m))
  ([]
   #(union-transformer model->dict)))

(defn lacinia-object-transformer
  []
  #(union-transformer model->object))

(defn postgres-table-transformer
  []
  #(union-transformer generate-table-edn))
