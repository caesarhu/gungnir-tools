(ns caesarhu.gungnir-tools.postgres.format
  (:require
    [caesarhu.gungnir-tools.utils :refer [comma-join-args to-sql-arg]]
    [honeysql-postgres.format]
    [honeysql.format :as sqlf :refer [fn-handler format-clause format-modifiers]]
    ; must require honeysql-postgres.format
    [honeysql.helpers :as sqlh]))


(defmethod fn-handler "generated" [_ & args]
  (let [generate-type (or (first args)
                          "ALWAYS")]
    (str "GENERATED " generate-type " AS IDENTITY")))


(defmethod fn-handler "references" [_ & args]
  (let [args-map (apply hash-map args)
        {:keys [table column on-delete on-update]} args-map
        base (str "REFERENCES " (sqlf/to-sql table) (comma-join-args (sqlh/collify column)))]
    (cond-> base
      on-delete (str " ON DELETE " (to-sql-arg on-delete))
      on-update (str " ON UPDATE " (to-sql-arg on-update)))))


(defmethod fn-handler "create-index" [_ & args]
  (let [args-map (apply hash-map args)
        {:keys [index-name table column unique]} args-map
        unique-val (if unique "UNIQUE " "")]
    (str "CREATE " unique-val "INDEX "
         (to-sql-arg index-name)
         " ON "
         (to-sql-arg table)
         (comma-join-args (sqlh/collify column))
         ";")))
