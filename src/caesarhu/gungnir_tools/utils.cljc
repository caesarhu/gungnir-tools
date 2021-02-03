(ns caesarhu.gungnir-tools.utils
  (:refer-clojure :exclude [pprint format partition-by])
  (:require [fipp.edn :refer [pprint]]
            [aero.core :as aero]
            [clojure.java.io :as io]
            [honeysql.format :as sqlf]
            [com.rpl.specter :as st]
            [camel-snake-kebab.core :as csk]
            [gungnir.model :as gm]
            [gungnir.field :as gf])

  (:import
    (java.io StringWriter)))

(defn read-edn-file
  [file]
  (aero/read-config (io/resource file)))

(defn pretty-format
  [obj]
  (with-open [w (StringWriter.)]
    (binding [*out* w]
      (pprint obj)
      (str w))))

(defn spit-object
  [path obj]
  (spit path (pretty-format obj)))

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

(defn transform-any-key
  "將函數f作用於coll中所有keyword，包含keys and vals."
  [coll f]
  (st/transform (st/walker keyword?) f coll))

(defn snake-any-key
  [coll]
  (transform-any-key coll csk/->snake_case_keyword))

(defn not-upsert?
  [field]
  (let [{:keys [primary-key auto virtual]} (gf/properties field)]
    (or virtual
        (and auto
             (not primary-key)))))

(defn not-upsert-fields
  [model]
  (let [{:keys [has-many belongs-to]} (gm/properties model)]
    (->> model
         gm/keys
         (map #(gm/child model %))
         (filter not-upsert?)
         (map first)
         (concat (keys has-many) (keys belongs-to)))))