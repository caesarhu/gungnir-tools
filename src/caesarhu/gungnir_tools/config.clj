(ns caesarhu.gungnir-tools.config
  (:require [malli.core :as m]))

(def translate-key*
  (atom :locale/zh-tw))

(def postgres-keys*
  (atom {:type-key :postgres/type}))

(def malli-type-keys*
  (atom (set (concat (keys (m/type-schemas))
                     [:local-date :local-date-time]))))