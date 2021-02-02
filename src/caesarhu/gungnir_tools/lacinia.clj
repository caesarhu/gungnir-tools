(ns caesarhu.gungnir-tools.lacinia
  (:require [caesarhu.gungnir-tools.gungnir.types :as types]
            [caesarhu.gungnir-tools.config :refer [translate-key*]]
            [medley.core :as medley]
            [malli.core :as m]
            [gungnir.model :as gm]
            [gungnir.field :as gf]))

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

(defn models->objects
  ([models]
   (->> models
        vals
        (map model->object)
        (apply merge)
        (hash-map :objects)))
  ([]
   (models->objects @gm/models)))

(defn models-with-query
  ([models query]
   (merge (models->objects models)
          query))
  ([query]
   (models-with-query @gm/models query)))
