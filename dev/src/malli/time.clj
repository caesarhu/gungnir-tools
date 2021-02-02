(ns malli.time
  (:require
    [clojure.test.check.generators :as gen]
    [java-time :as jt]
    [malli.core :as m]))


(defn date->str
  [d]
  (if (jt/local-date? d)
    (jt/format :iso-local-date d)
    d))


(defn str->date
  [s]
  (if (string? s)
    (jt/local-date s)
    s))


(defn date-time->str
  [t]
  (if (jt/local-date-time? t)
    (jt/format :iso-local-date t)
    t))


(defn str->date-time
  [s]
  (if (string? s)
    (jt/local-date-time s)
    s))


(defn rand-local-date
  ([start-date end-date]
   (let [start (.toEpochDay start-date)
         end (.toEpochDay end-date)
         rand-epoch (+ (rand-int (- end start)) start)]
     (java.time.LocalDate/ofEpochDay rand-epoch)))
  ([]
   (rand-local-date (jt/local-date 1920 1 1) (jt/local-date))))


(def gen-local-date
  (gen/fmap (fn [_]
              (rand-local-date))
            gen/large-integer))


(def gen-birthday
  (gen/fmap (fn [_]
              (rand-local-date (jt/local-date 1955 1 1) (jt/local-date 2002 12 31)))
            gen/large-integer))


(def gen-local-date-time
  (gen/fmap #(-> (java.time.Instant/ofEpochMilli %)
                 (java.time.LocalDateTime/ofInstant java.time.ZoneOffset/UTC))
            gen/large-integer))


(def gen-20years-local-date-time
  (gen/fmap (fn [_]
              (let [day-milli (* 60 60 24 1000)
                    epoch-day (->> (rand-local-date (jt/local-date 2000 1 1) (jt/local-date))
                                   jt/as-map
                                   :epoch-day)
                    epoch-malli (+ (* epoch-day day-milli)
                                   (rand-int day-milli))]
                (-> (java.time.Instant/ofEpochMilli epoch-malli)
                    (java.time.LocalDateTime/ofInstant java.time.ZoneOffset/UTC))))
            gen/large-integer))


(def local-date
  (m/-simple-schema
    {:type :local-date
     :pred jt/local-date?
     :type-properties {:error/message "should be java.time.LocalDate"
                       :decode/string str->date
                       :encode/string date->str
                       :decode/json str->date
                       :encode/json date->str
                       :json-schema/type "string"
                       :json-schema/format "date"
                       :gen/gen gen-birthday}}))


(def local-date-time
  (m/-simple-schema
    {:type            :local-date-time
     :pred            jt/local-date-time?
     :type-properties {:error/message      "should be java.time.LocalDateTime"
                       :decode/string      str->date-time
                       :encode/string      date-time->str
                       :decode/json        str->date-time
                       :encode/json        date-time->str
                       :json-schema/type   "string"
                       :json-schema/format "date-time"
                       :gen/gen gen-20years-local-date-time}}))


(def time-schema
  {:local-date local-date
   :local-date-time local-date-time})
