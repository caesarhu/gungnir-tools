(ns caesarhu.gungnir-tools.schema
  (:require [next.jdbc.types :as types]
            [caesarhu.gungnir-tools.utils :refer [read-edn-file]]
            [medley.core :as medley]
            [malli.core :as m]
            [malli.util :as mu]
            [malli.registry :as mr]
            [gungnir.model :as gm]
            [caesarhu.gungnir-tools.malli.time :as time]))

(def schema-registry* (atom nil))

(def schema-edn-file "schema.edn")

(defn read-edn-schema
  ([file]
   (read-edn-file file))
  ([]
   (read-edn-schema schema-edn-file)))

(defn register-model!
  ([file]
   (gm/register! (:model (read-edn-schema file))))
  ([]
   (register-model! schema-edn-file)))

;;; gungnir.model multimethods, only for enum values

(defmethod gm/before-save :enum/as-other [_k v]
  (types/as-other v))

;;; emuns def

(defn schema-enums
  ([file]
   (medley/map-kv-vals
     (fn [k v]
       (mu/update-properties v assoc :enum-name k))
     (:enum (read-edn-schema file))))
  ([]
   (schema-enums schema-edn-file)))

(defn enum-keys-set
  []
  (set (keys (schema-enums))))

(defn is-enum?
  [k]
  (when (keyword? k)
    (->> k name keyword (contains? (enum-keys-set)))))

;;; register function

(defn register-type!
  [type ?schema]
  (swap! schema-registry* assoc type ?schema))


(defn register-map!
  [m]
  (swap! schema-registry* merge m))

(defn base-schema
  ([file]
   (let [base (merge (m/default-schemas)
                     time/time-schema)]
     (reset! schema-registry* base)
     (mr/set-default-registry! (mr/mutable-registry schema-registry*))
     (register-map! (schema-enums file))
     @schema-registry*))
  ([]
   (base-schema schema-edn-file)))

