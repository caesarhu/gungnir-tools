(ns caesarhu.gungnir-tools.config
  (:require [malli.core :as m]))

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