(ns malli.employee
  (:require
    [clojure.test.check.generators :as gen]
    [malli.address :as addr]
    [malli.core :as m]
    [taiwan-id.core :as tid]))


(def taiwan-id
  (m/-simple-schema
    {:type            :string
     :pred            tid/some-id?
     :type-properties {:error/message "必須是身分證號或外籍證號"
                       :gen/gen       tid/taiwan-gen}}))


(def company-id
  [:re {:error/message "必須是員工編號-5位數字或Z開頭4位數字"} #"^[Z\d]\d{4}$"])


(def bank-id
  [:re {:error/message "必須是銀行代號-7位數字"} #"^\d{7}$"])


(def account
  [:re {:error/message "必須是銀行帳號-10位以上數字"} #"^\d{9}\d+$"])


(def family
  ["李", "王", "張", "劉", "陳", "楊", "黃", "趙", "周", "吳", "徐", "孫", "朱", "馬", "胡", "郭", "林", "何", "高", "梁", "鄭", "羅", "宋", "謝", "唐", "韓", "曹", "許", "鄧", "蕭", "馮", "曾", "程", "蔡", "彭", "潘", "袁", "於", "董", "餘", "蘇", "葉", "呂", "魏", "蔣", "田", "杜", "丁", "沈", "姜", "範", "江", "傅", "鐘", "盧", "汪", "戴", "崔", "任", "陸", "廖", "姚", "方", "金", "邱", "夏", "譚", "韋", "賈", "鄒", "石", "熊", "孟", "秦", "閻", "薛", "侯", "雷", "白", "龍", "段", "郝", "孔", "邵", "史", "毛", "常", "萬", "顧", "賴", "武", "康", "賀", "嚴", "尹", "錢", "施", "牛", "洪", "龔"])


(def given
  ["世", "中", "仁", "伶", "佩", "佳", "俊", "信", "倫", "偉", "傑", "儀", "元", "冠", "凱", "君", "哲", "嘉", "國", "士", "如", "娟", "婷", "子", "孟", "宇", "安", "宏", "宗", "宜", "家", "建", "弘", "強", "彥", "彬", "德", "心", "志", "忠", "怡", "惠", "慧", "慶", "憲", "成", "政", "敏", "文", "昌", "明", "智", "曉", "柏", "榮", "欣", "正", "民", "永", "淑", "玉", "玲", "珊", "珍", "珮", "琪", "瑋", "瑜", "瑞", "瑩", "盈", "真", "祥", "秀", "秋", "穎", "立", "維", "美", "翔", "翰", "聖", "育", "良", "芬", "芳", "英", "菁", "華", "萍", "蓉", "裕", "豪", "貞", "賢", "郁", "鈴", "銘", "雅", "雯", "霖", "青", "靜", "韻", "鴻", "麗", "龍"])


(def gen-name
  (gen/fmap (fn [rand]
              (let [family2 (shuffle family)
                    given2 (shuffle given)
                    raw-name (str (first family2) (first given2))]
                (if (< rand 10)
                  (str raw-name (second given2))
                  raw-name)))
            (gen/large-integer* {:min 1 :max 10})))


(def name-schema
  (m/-simple-schema
    {:type :string
     :pred string?
     :type-properties {:error/message "必須是姓名-2位以上字串"
                       :gen/gen gen-name}}))


(def unit-id
  [:re {:error/message "必須是單位代號-2~4位英數字"} #"^[a-zA-Z0-9]{2,4}$"])


(def mobile
  [:re {:error/message "必須是行動電話號碼-09+8位數字"} #"^09\d\d-\d{6}$"])


(def phone
  [:or {:postgres/type :string}
   [:re {:error/message "必須是電話號碼含區碼"} #"^02-[235-8]\d{7}$"]
   [:re {:error/message "必須是電話號碼含區碼"} #"^03-[2345689]\d{6}$"]
   [:re {:error/message "必須是電話號碼含區碼"} #"^037-\d{6}$"]
   [:re {:error/message "必須是電話號碼含區碼"} #"^04-[23]\d{7}$"]
   [:re {:error/message "必須是電話號碼含區碼"} #"^04-[78]\d{6}$"]
   [:re {:error/message "必須是電話號碼含區碼"} #"^049-\d{7}$"]
   [:re {:error/message "必須是電話號碼含區碼"} #"^05-[2-8]\d{6}$"]
   [:re {:error/message "必須是電話號碼含區碼"} #"^06-[2-79]\d{6}$"]
   [:re {:error/message "必須是電話號碼含區碼"} #"^07-\d{7}$"]
   [:re {:error/message "必須是電話號碼含區碼"} #"^089-\d{6}$"]
   [:re {:error/message "必須是電話號碼含區碼"} #"^08-[78]\d{6}$"]])


(def address
  (m/-simple-schema
    {:type :string
     :pred string?
     :type-properties {:error/message "必須是地址"
                       :gen/gen addr/gen-address}}))


(def employee-schema
  {:taiwan-id taiwan-id
   :employee/company-id company-id
   :employee/bank-id bank-id
   :employee/account account
   :taiwan-name name-schema
   :employee/unit-id unit-id
   :employee/phone phone
   :employee/mobile mobile
   :taiwan-address address})
