(ns caesarhu.gungnir-tools.config
  (:require [malli.core :as m]))

(defonce schema-registry* (atom nil))

(defonce schema-edn-file* (atom "schema.edn"))

(defonce tools-schema* (atom nil))

(defonce translate-key*
  (atom :locale/zh-tw))

(defonce assign-type-key*
  (atom :postgres/type))

(defonce postgres-keys*
  (atom {:column-call-set (set [:primary-key :postgres/unique :postgres/default :postgres/references])}))

(defonce ragtime-key*
  (atom :ragtime/id))

(defonce malli-type-keys*
  (atom (set (concat (keys (m/type-schemas))
                     [:local-date :local-date-time]))))