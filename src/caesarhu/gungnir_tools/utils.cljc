(ns caesarhu.gungnir-tools.utils
  (:refer-clojure :exclude [pprint format partition-by])
  (:require [fipp.edn :refer [pprint]]
            [honeysql.format :as sqlf])
  (:import
    (java.io StringWriter)))

(defn pretty-format
  [obj]
  (with-open [w (StringWriter.)]
    (binding [*out* w]
      (pprint obj)
      (str w))))

(def quote-symbol "'")

(defn quotation-str
  ([s quote]
   (if (string? s)
     (str quote s quote)
     (str s)))
  ([s]
   (quotation-str s quote-symbol)))

(defn comma-join-args
  "Returns the args comma-joined after applying to-sql to them"
  [args]
  (if (nil? args)
    ""
    (->> args
         (map sqlf/to-sql)
         sqlf/comma-join
         sqlf/paren-wrap)))

(defn to-sql-arg
  [arg]
  (if (string? arg)
    arg
    (sqlf/to-sql arg)))