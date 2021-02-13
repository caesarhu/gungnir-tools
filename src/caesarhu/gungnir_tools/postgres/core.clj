(ns caesarhu.gungnir-tools.postgres.core
  (:require
    [caesarhu.gungnir-tools.postgres.enum :refer [models-enum-edn] :as enum]
    [caesarhu.gungnir-tools.postgres.table :refer [models-table-edn] :as table]))


(defn generate-postgres-edn
  []
  (->> (concat (models-enum-edn) (models-table-edn))
       (sort-by :id)))
