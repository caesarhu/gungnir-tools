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
            [kaocha.repl :as k]
            [malli.time :refer [time-schema]]
            [malli.employee :refer [employee-schema]]
            [caesarhu.gungnir-tools.config :as config]
            [caesarhu.gungnir-tools.schema :as schema :refer [read-tools-schema]]
            [caesarhu.gungnir-tools.transform :as ct]
            [caesarhu.gungnir-tools.utils :refer [read-edn-file spit-object snake-any-key]]))

;;; test

(defn unit-test
  []
  (k/run :unit))

(def schema-file "schema.edn")

(defn init-schema!
  []
  (schema/base-schema)
  (schema/register-map! time-schema)
  (schema/register-map! employee-schema)
  (schema/register-model!))

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
  (set-init! (fn [] (read-tools-schema schema-file)))
  (juxt.clip.repl/reset)
  (read-tools-schema schema-file)
  (init-schema!)
  (instrument)
  (println "Reset finished..."))

(read-tools-schema schema-file)
(init-schema!)
;(instrument)
