(ns caesarhu.gungnir-tools.transform-test
  (:require [clojure.test :refer :all]
            [malli.core :as m]
            [gungnir.model :as gm]
            [caesarhu.gungnir-tools.transform :refer :all]))

(deftest union-transformer-test
  (testing "testing transform/union-transformer"
    (is (= {:銀行一覽表-test/銀行名稱-test :bank/name,
            :bank/memo :銀行一覽表-test/備註,
            :bank :銀行一覽表-test,
            :bank/bank-id :銀行一覽表-test/銀行代號,
            :bank/name :銀行一覽表-test/銀行名稱-test,
            :測試 :test,
            :銀行一覽表-test/備註 :bank/memo,
            :銀行一覽表-test/銀行代號 :bank/bank-id,
            :銀行一覽表-test :bank,
            :test :測試}
           (m/encode (:bank @gm/models)
                     [:map
                      {:locale/zh-tw "銀行一覽表-test"}
                      [:bank/name #:locale{:zh-tw "銀行名稱-test"} string?]]
                     (dict-transformer {:test :測試 :測試 :test}))))
    (is (= {:up ["CREATE TABLE employee (id bigint NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, taiwan_id text NOT NULL UNIQUE, company_id text NOT NULL UNIQUE, name text NOT NULL, birthday date NOT NULL, gender enum_gender NOT NULL, direct_kind enum_direct NOT NULL, employee_kind enum_employee NOT NULL, price_kind enum_price NOT NULL, reg_addr text NOT NULL, mail_addr text, unit_id text NOT NULL, bank_id text REFERENCES bank(bank_id) ON DELETE RESTRICT ON UPDATE CASCADE, account text, work_place text, factory text, job_title text, job_title_2 text, phone text, mobile text, education text, education_period text, exception bytea, memo text, created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP);"
                 "CREATE INDEX idx_employee_by_name ON employee(name);"],
            :down ["DROP TABLE IF EXISTS employee CASCADE;"],
            :id "201-employee"}
           (m/encode (:employee @gm/models)
                     nil
                     postgres-table-transformer)))))