(ns malli.address
  (:require
    [caesarhu.gungnir-tools.utils :refer [read-edn-file]]
    [clojure.java.io :as io]
    [clojure.string :as string]
    [clojure.test.check.generators :as gen]))


(defonce ^:private addr-seq
  (let [config (read-edn-file "config.edn")
        addr-file (io/resource (:address-path (:raw-data config)))]
    (-> addr-file
        (io/reader :encoding "utf16")
        (line-seq))))


(defn fake-number
  []
  (inc (rand-int 500)))


(defn fake-address-number
  []
  (let [lane (when (> (fake-number) 250)
               (str (fake-number) "巷"))
        alley (when (and lane (> (fake-number) 400))
                (str (inc (rand-int 15)) "弄"))
        sub (when (> (fake-number) 400)
              (str "之" (inc (rand-int 15))))
        number (str (fake-number) sub "號")
        floor (when (> (fake-number) 200)
                (str (inc (rand-int 20)) "樓"))]
    (str lane alley number floor)))


(defn fake-address
  []
  (let [addr-head (apply str (-> (rand-nth addr-seq)
                                 (string/split #",")
                                 drop-last
                                 rest))]
    (str addr-head (fake-address-number))))


(def gen-address
  (gen/fmap (fn [_]
              (fake-address))
            gen/large-integer))

