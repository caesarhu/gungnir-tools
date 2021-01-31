(ns user
  (:require [malli.core :as m]
            [malli.error :as me]
            [malli.generator :as mg]
            [malli.registry :as mr]
            [malli.util :as mu]
            [gungnir.changeset :as gc]
            [gungnir.database :as gd]
            [gungnir.field :as gf]
            [gungnir.model :as gm]
            [gungnir.query :as gq]
            [aero.core :as aero]
            [java-time :as jt]
            [malli.employee :refer [employee-schema]]
            [caesarhu.gungnir-tools.schema :as schema]))

(defn init-schema!
  ([file]
   (schema/base-schema file)
   (schema/register-map! employee-schema)
   (schema/register-model! file))
  ([]
   (init-schema! schema/schema-edn-file)))
