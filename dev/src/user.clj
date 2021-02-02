(ns user
  (:require [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [orchestra.spec.test :as stest]
            [juxt.clip.repl :refer [start stop set-init! system]]
            [malli.core :as m]
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
            [caesarhu.gungnir-tools.schema :as schema :refer [read-edn-schema]]
            [caesarhu.gungnir-tools.transform :as ct]
            [caesarhu.gungnir-tools.utils :refer [read-edn-file spit-object snake-any-key]]))

(defn init-schema!
  ([file]
   (schema/base-schema file)
   (schema/register-map! employee-schema)
   (schema/register-model! file))
  ([]
   (init-schema! schema/schema-edn-file)))

(init-schema!)

(def emp-t
  (read-edn-file "transform.edn"))

;;; expound and Orchestra

(defn unstrument
  []
  (stest/unstrument))


(defn instrument
  []
  (set! s/*explain-out* expound/printer)
  (with-out-str (stest/instrument))
  (println "starting strument..."))

(defn reset
  []
  (clojure.tools.namespace.repl/set-refresh-dirs "dev/src" "src" "test")
  (set-init! (fn [] (read-edn-schema)))
  (juxt.clip.repl/reset)
  (instrument)
  (println "Reset finished..."))