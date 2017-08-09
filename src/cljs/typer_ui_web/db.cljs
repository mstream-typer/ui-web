(ns typer-ui-web.db
  (:require [typer-ui-web.exercise.db :as exercise-db]
            [clojure.spec.alpha :as s]))


(s/def ::sheet-size
  (s/and int? pos?))


(s/def ::password
  string?)


(s/def ::username
  string?)


(s/def ::user
  (s/keys :req [::password ::username]))


(s/def ::id
  string?)


(s/def ::title
  string?)


(s/def ::description
  string?)


(s/def ::exercise-description
  (s/keys :req [::id ::title ::description]))


(s/def ::course
  (s/coll-of ::exercise-description :kind vector?))


(s/def ::view
  #{::home ::exercise})


(s/def ::visible
  boolean?)


(s/def ::loader
  (s/keys :req [::visible]))


(s/def ::login-menu (s/keys :req [::visible
                                  ::username
                                  ::password]))


(s/def ::main-menu nil?)


(s/def ::ui
  (s/keys :req [::loader ::login-menu ::main-menu ::view]))


(s/def ::db
  (s/keys :req [::exercise-db/exercise ::ui ::user]))


(def default-db
  (merge exercise-db/default-db
         {::user {::username ""
                  ::password ""}
          ::course [{::id "1"
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
                     ::description "description"}]
          ::ui {::loader {::visible false}
                ::view ::home  
                ::login-menu {::visible false
                              ::username ""
                              ::password ""}
                ::main-menu nil}}))
