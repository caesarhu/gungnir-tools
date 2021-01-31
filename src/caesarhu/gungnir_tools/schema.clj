(ns caesarhu.gungnir-tools.schema
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [next.jdbc.types :as types]
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
   (aero/read-config (io/resource file)))
  ([]
   (read-edn-schema schema-edn-file)))

(defn register-model!
  ([file]
   (gm/register! (:model (read-edn-schema file))))
  ([]
   (register-model! schema-edn-file)))

;;; gungnir.model multimethods

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
     (merge base (schema-enums file))))
  ([]
   (base-schema schema-edn-file)))

