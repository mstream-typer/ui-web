(ns typer-ui-web.db
  (:require [typer-ui-web.course.db :as course-db]
            [typer-ui-web.exercise.db :as exercise-db]
            [clojure.spec.alpha :as s]))


(s/def ::sheet-size
  (s/and int?
         pos?))


(s/def ::password
  string?)


(s/def ::username
  string?)


(s/def ::user
  (s/keys :req [::password
                ::username]))


(s/def ::view
  #{::home
    ::exercise})


(s/def ::visible
  boolean?)


(s/def ::loader
  (s/keys :req [::visible]))


(s/def ::login-menu (s/keys :req [::visible
                                  ::username
                                  ::password]))


(s/def ::user-dropdown
  (s/keys :req [::visible]))


(s/def ::main-menu
  (s/keys :req [::user-dropdown]))


(s/def ::ui
  (s/keys :req [::loader
                ::login-menu
                ::main-menu
                ::view]))


(s/def ::db
  (s/keys :req [::course-db/course
                ::exercise-db/exercise
                ::ui
                ::user]))


(def default-db
  (merge course-db/default-db
         exercise-db/default-db
         {::user {::username ""
                  ::password ""}
          ::ui {::loader {::visible false}
                ::view ::home  
                ::login-menu {::loader {::visible false}
                              ::visible false
                              ::username ""
                              ::password ""}
                ::main-menu {::user-dropdown {::visible false}}}}))
