(ns caesarhu.gungnir-tools.transform
  (:require
    [caesarhu.gungnir-tools.gungnir.translate :refer [model->dict]]
    [caesarhu.gungnir-tools.lacinia :refer [model->object]]
    [caesarhu.gungnir-tools.postgres.table :refer [generate-table-edn]]
    [clojure.spec.alpha :as s]
    [malli.transform :as mt]
    [malli.util :as mu]))


(s/fdef union-transformer
        :args (s/alt :arity-2 (s/cat :f (s/nilable fn?)
                                     :m (s/nilable map?))
                     :arity-1 (s/cat :f-or-m (s/or :function (s/nilable fn?)
                                                   :map (s/nilable map?)))
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
