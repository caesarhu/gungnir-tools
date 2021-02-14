(ns caesarhu.gungnir-tools.generator-test
  (:require
    [aero.core :as aero]
    [caesarhu.gungnir-tools.lacinia :refer :all]
    [caesarhu.gungnir-tools.postgres.table :refer :all]
    [caesarhu.gungnir-tools.test-helpers :refer :all]
    [clojure.test :refer :all]
    [gungnir.model :as gm]
    [malli.core :as m]))


(use-fixtures
  :once
  fixtures)


(deftest lacinia-test
  (testing "lacinia graphql edn generator"
    (is (= (aero/read-config "test/caesarhu/gungnir_tools/lacinia-graphql.edn")
           (models->objects)))))


(deftest postgres-test
  (testing "postgres edn generator"
    (is (= (aero/read-config "test/caesarhu/gungnir_tools/postgres.edn")
           (vec (models-table-edn))))))
