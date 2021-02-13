(ns caesarhu.gungnir-tools.gungnir.upsert
  (:require
    [caesarhu.unqualify :refer [unqualify]]
    [gungnir.database :as gd]
    [gungnir.model :as gm]
    [honeysql-postgres.helpers :as psqlh]
    [honeysql.helpers :as sql]))


(defn upsert-sql
  [table row conflict]
  (-> (sql/insert-into table)
      (sql/values [row])
      (psqlh/upsert (apply psqlh/do-update-set
                           (psqlh/on-conflict {} (unqualify conflict))
                           (->> (keys row)
                                (map unqualify))))))


(defn upsert!
  "Upsert a row based on the `changeset` provided. This function assumes
  that the `:changeset/result` key does not have a primary-key with a
  values. Returns the inserted row on succes. If insert conflicted, do update on table,
  On failure return the `changeset` with an updated `:changeset/errors` key."
  ([changeset conflict] (upsert! changeset conflict gd/*datasource*))
  ([{:changeset/keys [model errors result] :as changeset} conflict datasource]
   (if errors
     changeset
     (let [sql (upsert-sql (gm/table model) (#'gungnir.database/record->insert-values result) conflict)
           res (#'gungnir.database/execute-one! sql changeset datasource)]
       (if (:changeset/errors res)
         res
         (#'gungnir.database/process-query-row {:select '(:*)} datasource res))))))
