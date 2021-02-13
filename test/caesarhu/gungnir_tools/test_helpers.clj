(ns caesarhu.gungnir-tools.test-helpers
  (:require
    [caesarhu.gungnir-tools.schema :as schema]
    [clojure.spec.alpha :as s]
    [clojure.test :refer :all]
    [expound.alpha :as expound]
    [malli.employee :refer [employee-schema]]
    [orchestra.spec.test :as stest]))


(def schema-file "schema.edn")


(defn init-schema!
  []
  (schema/base-schema schema-file)
  (schema/register-map! employee-schema)
  (schema/register-model!))


(defn fixtures
  [f]
  (set! s/*explain-out* expound/printer)
  (stest/instrument)
  (init-schema!)
  (f))
