(ns caesarhu.gungnir-tools.postgres.table
  (:require
    [caesarhu.gungnir-tools.config :refer [postgres-keys* ragtime-key* tools-schema*]]
    [caesarhu.gungnir-tools.gungnir.types :refer [->postgres-type field-type]]
    [caesarhu.gungnir-tools.postgres.format]
    [caesarhu.gungnir-tools.utils :as utils]
    [clojure.spec.alpha :as s]
    [gungnir.field :as gf]
    [gungnir.model :as gm]
    [gungnir.spec]
    [honeysql-postgres.helpers :as psqlh]
    [honeysql.core :as sql]
    [malli.core :as m]))


(defn field->postgres-type
  [field]
  (-> field field-type ->postgres-type))


(defn column-null
  [field]
  (when-not (= :maybe (m/type (last field)))
    (sql/call :not nil)))


(defn column-auto
  [field]
  (let [properties (gf/properties field)
        f-type (field->postgres-type field)
        get-property #(get properties %)]
    (when (get-property :auto)
      (cond
        (and (get-property :primary-key)
             (= :bigint f-type)) (sql/call :generated)
        (= :date f-type) (sql/call :default :CURRENT_DATE)
        (= :timestamp f-type) (sql/call :default :CURRENT_TIMESTAMP)))))


(defn sql-call
  [property-key property]
  (when property
    (let [s-key (-> property-key name keyword)]
      (cond
        (true? property) (sql/call s-key)
        (coll? property) (apply sql/call s-key property)
        :else (sql/call s-key property)))))


(defn column-sql-call
  [field]
  (let [properties (gf/properties field)
        column-call-set (get @postgres-keys* :column-call-set)
        get-property #(get properties %)
        column-attr (fn [property-key]
                      (let [property (get-property property-key)]
                        (when (contains? column-call-set property-key)
                          (sql-call property-key property))))]
    (->> (map column-attr (keys properties))
         (filter some?))))


(defn field->column
  [field]
  (let [args-fn (fn [field]
                  (->> ((juxt column-null column-auto column-sql-call) field)
                       flatten
                       (filter some?)))]
    (when-not (:virtual (gf/properties field))
      (->> ((juxt first field->postgres-type args-fn) field)
           flatten
           vec))))


(s/fdef model-columns
        :args (s/cat :model :gungnir/model))


(defn model-columns
  [model]
  (->> model
       gm/keys
       (map #(gm/child model %))
       (map field->column)
       (filter some?)))


(s/fdef create-table
        :args (s/cat :model :gungnir/model))


(defn create-table
  [model]
  (let [sql-map (-> (psqlh/create-table {} (gm/table model))
                    (psqlh/with-columns (model-columns model)))]
    (-> (sql/format sql-map :parameterizer :none)
        first
        (str ";"))))


(s/fdef drop-table
        :args (s/cat :model :gungnir/model))


(defn drop-table
  [model]
  (str "DROP TABLE IF EXISTS "
       (-> model gm/table utils/to-sql-arg)
       " CASCADE;"))


(s/fdef create-index
        :args (s/cat :model :gungnir/model))


(defn create-index
  [model]
  (let [index-key (get-in @tools-schema* [:postgres-keys :index-key])]
    (when-let [index-property (-> (gm/properties model)
                                  index-key)]
      (if (coll? (first index-property))
        (->> (map #(apply sql/call index-key %) index-property)
             (map sql/format)
             flatten)
        (->> (apply sql/call index-key index-property)
             sql/format)))))


(s/fdef generate-table-edn
        :args (s/cat :model :gungnir/model))


(defn generate-table-edn
  [model]
  (let [base {:up (->> (vector (create-table model) (create-index model))
                       flatten
                       (filter some?)
                       vec)
              :down (vector (drop-table model))}
        id (get (gm/properties model) @ragtime-key*)]
    (if id
      (assoc base :id id)
      base)))


(s/fdef models-table-edn
        :args (s/alt :arity-1
                     (s/cat :models (s/coll-of :gungnir/model))
                     :arity-0
                     (s/cat)))


(defn models-table-edn
  ([models]
   (map generate-table-edn models))
  ([]
   (models-table-edn (vals @gm/models))))
