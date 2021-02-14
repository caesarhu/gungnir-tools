(ns caesarhu.gungnir-tools.gungnir.types
  (:require
    [caesarhu.gungnir-tools.config :refer [postgres-keys* malli-type-keys* assign-type-key*]]
    [caesarhu.gungnir-tools.postgres.enum :as enum]
    [caesarhu.gungnir-tools.schema :refer [get-schemas is-enum?]]
    [malli.core :as m]
    [malli.registry :as mr]))


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

(def type-transfrom-table
  [{:malli-types (set [:re :string 'string?]) :graphql-type 'String :postgres-type :text}
   {:malli-types (set ['integer?, 'int?, 'pos-int?, 'neg-int?, 'nat-int?, :int])
    :graphql-type 'Int :postgres-type :bigint}
   {:malli-types (set ['float?, 'double?, 'decimal?, :double]) :graphql-type 'BigDecimal :postgres-type :decimal}
   {:malli-types (set [:date]) :graphql-type 'Date :postgres-type :date}
   {:malli-types (set [:date-time 'inst?]) :graphql-type 'DateTime :postgres-type :timestamp}
   {:malli-types (set [:boolean 'boolean?]) :graphql-type 'Boolean :postgres-type :boolean}
   {:malli-types (set ['bytes?]) :graphql-type 'String :postgres-type :bytea}])

(defn find-type
  [type transform-key entry]
  (let [malli-types (:malli-types entry)]
    (when (contains? malli-types type)
      (get entry transform-key))))

(defn transform-type
  [type transform-key]
  (some #(find-type type transform-key %) type-transfrom-table))

(defn ->graphql-type
  [type]
  (cond
    (transform-type type :graphql-type) (transform-type type :graphql-type)
    (is-enum? type) 'String
    :else (throw (ex-info "field graphql-type parse error!"
                          {:cause ::->graphql-type
                           :type type}))))

(defn ->postgres-type
  [type]
  (cond
    (transform-type type :postgres-type) (transform-type type :postgres-type)
    (is-enum? type) type
    :else (throw (ex-info "field postgres-type parse error!"
                          {:cause ::>postgres-type
                           :type type}))))
