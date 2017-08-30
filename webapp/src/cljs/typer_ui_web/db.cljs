(ns typer-ui-web.db
  (:require [typer-ui-web.course.db :as course-db]
            [typer-ui-web.exercise.db :as exercise-db]
            [typer-ui-web.main-menu.db :as main-menu-db]
            [clojure.spec.alpha :as s]))


(s/def ::view
  #{::home
    ::course
    ::exercise})


(s/def ::ui
  (s/keys :req [::view]))


(s/def ::db
  (s/keys :req [::course-db/course
                ::exercise-db/exercise
                ::main-menu-db/main-menu
                ::ui]))


(def default-db
  (merge course-db/default-db
         exercise-db/default-db
         main-menu-db/default-db
         {::ui {::view ::home}}))
