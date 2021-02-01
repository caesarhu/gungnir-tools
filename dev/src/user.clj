(ns user
  (:require [malli.core :as m]
            [malli.error :as me]
            [malli.generator :as mg]
            [malli.registry :as mr]
            [malli.transform :as mt]
            [malli.util :as mu]
            [gungnir.changeset :as gc]
            [gungnir.database :as gd]
            [gungnir.field :as gf]
            [gungnir.model :as gm]
            [gungnir.query :as gq]
            [aero.core :as aero]
            [java-time :as jt]
            [malli.employee :refer [employee-schema]]
            [caesarhu.gungnir-tools.schema :as schema]
            [caesarhu.gungnir-tools.transform :as ct]
            [caesarhu.gungnir-tools.utils :refer [read-edn-file]]))

(defn init-schema!
  ([file]
   (schema/base-schema file)
   (schema/register-map! employee-schema)
   (schema/register-model! file))
  ([]
   (init-schema! schema/schema-edn-file)))

(def emp-t
  (read-edn-file "transform.edn"))
