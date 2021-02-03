(ns caesarhu.gungnir-tools.config
  (:require [malli.core :as m]))

(defonce schema-registry* (atom nil))

(defonce schema-edn-file* (atom "schema.edn"))

(defonce tools-schema* (atom nil))

(defonce translate-key*
  (atom :locale/zh-tw))

(defonce postgres-keys*
  (atom {:type-key :postgres/type
         :column-call-set (set [:primary-key :unique :default :references])}))

(defonce ragtime-key*
  (atom :ragtime/id))

(defonce malli-type-keys*
  (atom (set (concat (keys (m/type-schemas))
                     [:local-date :local-date-time]))))