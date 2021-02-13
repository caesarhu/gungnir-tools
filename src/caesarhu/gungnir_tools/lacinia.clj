(ns caesarhu.gungnir-tools.lacinia
  (:require
    [caesarhu.gungnir-tools.config :refer [translate-key*]]
    [caesarhu.gungnir-tools.gungnir.types :as types]
    [clojure.spec.alpha :as s]
    [gungnir.field :as gf]
    [gungnir.model :as gm]
    [gungnir.spec]
    [malli.core :as m]
    [medley.core :as medley]))


(s/fdef ->graphql-field
        :args (s/cat :field :gungnir.model/field))


(defn ->graphql-field
  [field]
  (let [{:keys [locale/zh-tw optional]} (gf/properties field)
        non-null (fn [type]
                   (if (or optional
                           (= :maybe (m/type (last field))))
                     type
                     (list (symbol :non-null) type)))
        graphql-type (-> field types/field-type types/->graphql-type non-null)]
    (hash-map (first field) {:type graphql-type :description zh-tw})))


(s/fdef relations
        :args (s/cat :model :gungnir/model))


(defn relations
  [model]
  (let [{:keys [has-many has-one belongs-to]} (gm/properties model)
        ->relation (fn [[k v]]
                     {k {:type (:model v)}})
        ->list (fn [type] (list (symbol :list) type))]
    (->> [(map ->relation has-one)
          (map ->relation belongs-to)
          (->> (map ->relation has-many)
               (apply merge)
               (medley/map-vals #(update % :type ->list)))]
         flatten
         (apply merge))))


(s/fdef model->object
        :args (s/cat :model :gungnir/model))


(defn model->object
  [model]
  (let [desc (@translate-key* (gm/properties model))]
    (->> model
         gm/keys
         (map #(gm/child model %))
         (map ->graphql-field)
         (apply merge (relations model))
         (hash-map :description desc :fields)
         (hash-map (gm/table model)))))


(s/fdef models->objects
        :args (s/alt :1arity
                     (s/cat :models (s/coll-of :gungnir/model))
                     :0arity
                     (s/cat)))


(defn models->objects
  ([models]
   (->> models
        (map model->object)
        (apply merge)
        (hash-map :objects)))
  ([]
   (models->objects (vals @gm/models))))
