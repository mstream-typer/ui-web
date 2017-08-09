(ns typer-ui-web.course.db
  (:require [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]))


(def dummy-exercises [{::id "1"
                       ::title "f & j"
                       ::description "description"}
                      {::id "2"
                       ::title "d & k"
                       ::description "description"}
                      {::id "3"
                       ::title "s & l"
                       ::description "description"}
                      {::id "4"
                       ::title "a & ;"
                       ::description "description"}
                      {::id "5"
                       ::title "e & i"
                       ::description "description"}])


(s/def ::id
  string?)


(s/def ::title
  string?)


(s/def ::description
  string?)


(s/def ::exercise
  (s/keys :req [::id ::title ::description]))


(s/def ::course
  (s/coll-of ::exercise :kind vector?))

(def default-db
  {::course {::data {::exercises dummy-exercises}
             ::ui {}}})
                       
