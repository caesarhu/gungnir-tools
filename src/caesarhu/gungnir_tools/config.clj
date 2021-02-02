(ns caesarhu.gungnir-tools.config
  (:require [malli.core :as m]))

(def translate-key*
  (atom :locale/zh-tw))

(def postgres-keys*
  (atom {:type-key :postgres/type
         :column-call-set (set [:primary-key :unique :default :references])}))

(def ragtime-key*
  (atom :ragtime/id))

(def malli-type-keys*
  (atom (set (concat (keys (m/type-schemas))
                     [:local-date :local-date-time]))))