(ns rubyfuza
  (:require [datomic.api :as d]))

;; Create a database

(def uri "datomic:mem://sample-database")
(d/delete-database uri) ;; just in case I mess up
(d/create-database uri)


;; Connect

(def conn (d/connect uri))


;; Define schema. This is just Clojure data!

(def schema
  [{:db/ident       :user/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/index       true
    ;; Install as schema
    :db/id (d/tempid :db.part/db) :db.install/_attribute :db.part/db}

   {:db/ident       :group/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/index       true
    :db/id (d/tempid :db.part/db) :db.install/_attribute :db.part/db}

   {:db/ident       :group/users
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/id (d/tempid :db.part/db) :db.install/_attribute :db.part/db}])


;; Transact it. Block until the result is ready, then read it.

@(d/transact conn schema)


;; Define some data

(def data
  (let [u1 {:db/id (d/tempid :db.part/user) :user/name "Robert"}
        u2 {:db/id (d/tempid :db.part/user) :user/name "Marc"}
        u3 {:db/id (d/tempid :db.part/user) :user/name "Aslam"}
        u4 {:db/id (d/tempid :db.part/user) :user/name "DHH"}
        users [u1 u2 u3 u4]]
    (conj users
          {:db/id       (d/tempid :db.part/user)
           :group/name  "RubyFuza 2014 Speakers"
           :group/users (map :db/id users)})))

@(d/transact conn data)


;; Get a value for the current database

(def db1 (d/db conn))


;; Read the index directly - great for simple lookups

(seq (d/datoms db1 :avet :user/name))
(seq (d/datoms db1 :avet :user/name "Aslam"))
(seq (d/datoms db1 :aevt :group/users))


;; Oops. DHH can't make it.

(defn get-first-e
  [datoms]
  (:e (first (seq datoms))))

(def rubyfuza-id (get-first-e (d/datoms db1 :avet :group/name "RubyFuza 2014 Speakers")))
(def dhh-id (get-first-e (d/datoms db1 :avet :user/name "DHH")))

(def remove-dhh [[:db/retract rubyfuza-id :group/users dhh-id]])

@(d/transact conn remove-DHH)
(def db2 (d/db conn))


;; Take another look:

(seq (d/datoms db2 :avet :user/name))
(seq (d/datoms db2 :eavt rubyfuza-id :group/users))


;; What about our previous database?

(seq (d/datoms db1 :eavt rubyfuza-id :group/users))


;; The database is a value. It's immutable. You can always go back in time!




;; Entity API - entities as lazily-loading maps

(def aslam-id (get-first-e (d/datoms db2 :avet :user/name "Aslam")))
(def aslam (d/entity db2 aslam-id))


;; Keywords are functions, too. Same as (get aslam :user/name)

(:user/name aslam)

(def rubyfuza (d/entity db2 rubyfuza-id))

(:group/name rubyfuza)
(:group/users rubyfuza)

(sort (map :user/name (:group/users rubyfuza)))


;; Relationships are implicitly bi-directional!

(:group/name (first (:group/_users aslam)))


;; Datalog for more complex queries

(d/q '[:find ?user-id ?user-name
       :in $ ?group-name
       :where
       [?group-id :group/name ?group-name]
       [?group-id :group/users ?user-id]
       [?user-id :user/name ?user-name]]
     db2
     "RubyFuza 2014 Speakers")

