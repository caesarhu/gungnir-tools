(ns caesarhu.gungnir-tools.gungnir.types
  (:require [malli.core :as m]
            [caesarhu.gungnir-tools.schema :refer [schema-registry* is-enum?]]
            [caesarhu.gungnir-tools.postgres.enum :as enum]
            [caesarhu.gungnir-tools.config :refer [postgres-keys* malli-type-keys*]]))

(defn field-type
  [field]
  (loop [schema (last field)]
    (let [f-type (or (some-> schema
                             m/properties
                             (get (:type-key @postgres-keys*)))
                     (m/type schema))]
      (cond
        (symbol? f-type) f-type
        (contains? @malli-type-keys* f-type) f-type
        (= :re f-type) f-type
        (= :enum f-type) (enum/get-enum-name schema)
        (= :malli.core/schema f-type) (recur (->> schema m/form (get @schema-registry*)))
        (= :maybe f-type) (recur (->> schema m/children first))
        :else (throw (ex-info "field type parse error!"
                              {:cause ::field-type
                               :schema schema}))))))

(defn transform-type
  [type-table type]
  (let [transform (fn [[type-set to-type]]
                    (when (type-set type)
                      to-type))]
    (some transform type-table)))


(def graphql-type-table
  [[(set [:re :string 'string?]) (symbol :String)]
   [(set ['integer?, 'int?, 'pos-int?, 'neg-int?, 'nat-int?, :int]) (symbol :Int)]
   [(set ['float?, 'double?, 'decimal?, :double]) (symbol :BigDecimal)]
   [(set [:local-date]) (symbol :Date)]
   [(set [:local-date-time 'inst?]) (symbol :DateTime)]
   [(set [:boolean 'boolean?]) (symbol :Boolean)]
   [(set ['bytes?]) (symbol :String)]])

(defn ->graphql-type
  [type]
  (cond
    (transform-type graphql-type-table type) (transform-type graphql-type-table type)
    (is-enum? type) (symbol :String)
    :else (throw (ex-info "field type graphql-type parse error!"
                          {:cause ::->graphql-type
                           :type type}))))