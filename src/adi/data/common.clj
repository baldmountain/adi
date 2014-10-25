(ns adi.data.common
  (:require [datomic.api :as d]))

(defn iid
  "Constructs a new datomic db/id
  (iid 1) => #db/id[:db.part/user -1]

  (iid :hello) => #db/id[:db.part/user -245025397]"
  {:added "0.3"}
  ([] (d/tempid :db.part/user))
  ([obj]
     (let [v (if (number? obj) (long obj) (hash obj))
           v (if (< 0 v) (- v) v)]
       (d/tempid :db.part/user v ))))

(defn isym
  "Returns a new datomic symbol with a unique name. If a prefix string
  is supplied, the name is `prefix#` where `#` is some unique number. If
  prefix is not supplied, the prefix is `e_`.

  (isym) => => ?e_1238

  (isym \"v\") => => ?v1250
  "
  {:added "0.3"}
  ([] (isym 'e_))
 ([prefix] (symbol (str "?" (name (gensym prefix))))))