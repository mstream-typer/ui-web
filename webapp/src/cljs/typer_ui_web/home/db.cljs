(ns typer-ui-web.home.db
  (:require [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [typer-ui-web.common.db :as common-db]))


(s/def ::name
  string?)


(s/def ::description
  string?)


(s/def ::course
  (s/keys :req [::common-db/id
                ::name
                ::description]))


(s/def ::courses
  (s/coll-of ::course :kind vector?))


(s/def ::data
  (s/keys :req [::courses]))


(s/def ::ui
  (s/keys :req [::common-db/loader]))


(s/def ::home
  (s/keys :req [::data ::ui]))


(def default-db
  {::home {::data {::courses []}
           ::ui {::common-db/loader {::common-db/visible false}}}})
