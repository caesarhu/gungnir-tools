(ns caesarhu.schema
  (:require [aero.core :as aero]
            [clojure.java.io :as io]))

(def schema-edn "schema.edn")

(defn read-edn-schema
  ([file]
   (aero/read-config (io/resource file)))
  ([]
   (read-edn-schema schema-edn)))