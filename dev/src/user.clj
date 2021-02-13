(ns user
  (:require
    [aero.core :as aero]
    [caesarhu.gungnir-tools.config :as config]
    [caesarhu.gungnir-tools.schema :as schema :refer [read-tools-schema]]
    [caesarhu.gungnir-tools.transform :as ct]
    [caesarhu.gungnir-tools.utils :refer [read-edn-file spit-object snake-any-key]]
    [caesarhu.malli-tools.time :as time]
    [clojure.spec.alpha :as s]
    [clojure.tools.gitlibs :as gl]
    [expound.alpha :as expound]
    [gungnir.changeset :as gc]
    [gungnir.database :as gd]
    [gungnir.field :as gf]
    [gungnir.model :as gm]
    [gungnir.query :as gq]
    [java-time :as jt]
    [juxt.clip.repl :refer [start stop set-init! system]]
    [kaocha.repl :as k]
    [malli.core :as m]
    [malli.employee :refer [employee-schema]]
    [malli.error :as me]
    [malli.generator :as mg]
    [malli.registry :as mr]
    [malli.transform :as mt]
    [malli.util :as mu]
    [orchestra.spec.test :as stest]))

;;; test

(defn unit-test
  []
  (k/run :unit))


(def schema-file "schema.edn")


(defn init-schema!
  []
  (schema/base-schema schema-file)
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
  (set-init! (fn []))
  (juxt.clip.repl/reset)
  (init-schema!)
  (instrument)
  (println "Reset finished..."))

;(init-schema!)

