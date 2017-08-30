(ns typer-ui-web.course.db
  (:require [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [typer-ui-web.common.db :as common-db]))


(s/def ::name
  string?)


(s/def ::description
  string?)


(s/def ::exercise
  (s/keys :req [::common-db/id
                ::name
                ::description]))


(s/def ::exercises
  (s/coll-of ::exercise :kind vector?))


(s/def ::data
  (s/keys :req [::exercises]))


(s/def ::ui
  (s/keys :req [::common-db/loader]))


(s/def ::course
  (s/keys :req [::data ::ui]))


(def default-db
  {::course {::data {::name ""
                     ::exercises []}
             ::ui {::common-db/loader {::common-db/visible true}}}})

                       
