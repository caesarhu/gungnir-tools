(ns caesarhu.gungnir-tools.schema
  (:require
    [caesarhu.gungnir-tools.config :as config]
    [caesarhu.gungnir-tools.utils :refer [read-edn-file]]
    [gungnir.model :as gm]
    [malli.core :as m]
    [malli.registry :as mr]
    [malli.util :as mu]
    [medley.core :as medley]))


(defn read-tools-schema
  [file]
  (let [schema (eval (read-edn-file file))
        {:keys [translate-key assign-type-key ragtime-key postgres-keys extra-type]} schema
        custom-types (set (concat (keys (m/type-schemas))
                                  (keys extra-type)))]
    (reset! config/tools-schema* schema)
    (reset! config/translate-key* translate-key)
    (reset! config/assign-type-key* assign-type-key)
    (reset! config/ragtime-key* ragtime-key)
    (reset! config/postgres-keys* postgres-keys)
    (reset! config/malli-type-keys* custom-types)))


(defn register-model!
  []
  (gm/register! (:model @config/tools-schema*)))

;;; emuns def

(defn schema-enums
  []
  (medley/map-kv-vals
    (fn [k v]
      (mu/update-properties v assoc :enum-name k))
    (:enum @config/tools-schema*)))


(defn enum-keys-set
  []
  (set (keys (schema-enums))))


(defn is-enum?
  [k]
  (when (keyword? k)
    (->> k name keyword (contains? (enum-keys-set)))))

;;; register function

(defn register-type!
  [?schema type]
  (swap! config/schema-registry* assoc type ?schema))


(defn register-map!
  [m]
  (swap! config/schema-registry* merge m))


(defn base-schema
  [file]
  (read-tools-schema file)
  (reset! config/schema-registry* (m/default-schemas))
  (mr/set-default-registry! (mr/mutable-registry config/schema-registry*))
  (register-map! (schema-enums))
  (register-map! (:extra-type @config/tools-schema*))
  @config/schema-registry*)


(defn get-schemas
  []
  (mr/schemas m/default-registry))
