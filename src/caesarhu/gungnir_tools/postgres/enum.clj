(ns caesarhu.gungnir-tools.postgres.enum
  (:require
    [caesarhu.gungnir-tools.utils :as utils]
    [caesarhu.gungnir-tools.config :refer [ragtime-key*]]
    [malli.core :as m]))

(defn get-enum-name
  [enum]
  (-> enum
      m/properties
      :enum-name))


(defn create-enum
  [enum]
  (let [values (m/children enum)
        values-str (->> values
                        (map utils/quotation-str)
                        (map keyword)
                        utils/comma-join-args)]
    (str "CREATE TYPE "
         (utils/to-sql-arg (get-enum-name enum))
         " AS ENUM "
         values-str
         ";")))


(defn drop-enum
  [enum]
  (str "DROP TYPE IF EXISTS "
       (utils/to-sql-arg (get-enum-name enum))
       " CASCADE;"))


(defn generate-enum-edn
  [enum]
  (let [base {:up (vector (create-enum enum))
              :down (vector (drop-enum enum))}
        id (get (m/properties enum) @ragtime-key*)]
    (if id
      (assoc base :id id)
      base)))

