(ns caesarhu.gungnir-tools.gungnir.types
  (:require [malli.core :as m]
            [malli.registry :as mr]
            [caesarhu.gungnir-tools.schema :refer [get-schemas is-enum?]]
            [caesarhu.gungnir-tools.postgres.enum :as enum]
            [caesarhu.gungnir-tools.config :refer [postgres-keys* malli-type-keys* assign-type-key*]]))

(defn field-type
  [field]
  (loop [schema (last field)]
    (let [f-type (or (some-> schema
                             m/properties
                             (get @assign-type-key*))
                     (m/type schema))]
      (cond
        (symbol? f-type) f-type
        (contains? @malli-type-keys* f-type) f-type
        (= :re f-type) f-type
        (= :enum f-type) (enum/get-enum-name schema)
        ;(= :malli.core/schema f-type) (recur (->> schema m/form (get (get-schemas))))
        (= :malli.core/schema f-type) (recur (mr/schema m/default-registry (m/form schema)))
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
    :else (throw (ex-info "field graphql-type parse error!"
                          {:cause ::->graphql-type
                           :type type}))))

(def postgres-type-table
  [[(set [:re :string 'string?]) :text]
   [(set ['integer?, 'int?, 'pos-int?, 'neg-int?, 'nat-int?, :int]) :bigint]
   [(set ['float?, 'double?, 'decimal?, :double]) :decimal]
   [(set [:local-date]) :date]
   [(set [:local-date-time 'inst?]) :timestamp]
   [(set [:boolean 'boolean?]) :boolean]
   [(set ['bytes?]) :bytea]])

(defn ->postgres-type
  [type]
  (cond
    (transform-type postgres-type-table type) (transform-type postgres-type-table type)
    (is-enum? type) type
    :else (throw (ex-info "field postgres-type parse error!"
                          {:cause ::>postgres-type
                           :type type}))))