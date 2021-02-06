(ns caesarhu.gungnir-tools.schema
  (:require [caesarhu.gungnir-tools.utils :refer [read-edn-file]]
            [caesarhu.gungnir-tools.config :as config]
            [medley.core :as medley]
            [malli.core :as m]
            [malli.util :as mu]
            [malli.registry :as mr]
            [gungnir.model :as gm]))

(defn read-tools-schema
  [file]
  (reset! config/tools-schema* (read-edn-file file)))

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
  []
  (reset! config/schema-registry* (m/default-schemas))
  (mr/set-default-registry! (mr/mutable-registry config/schema-registry*))
  (register-map! (schema-enums))
  @config/schema-registry*)

(defn get-schemas
  []
  (mr/schemas m/default-registry))