(ns typer-ui-web.exercise.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [typer-ui-web.course.db :as course-db]
            [clojure.spec.alpha :as s] 
            [re-frame.core :as rf]))


(rf/reg-sub
 ::exercises
 #(-> %
      (::course-db/exercise)
      (::course-db/data)
      (::course-db/exercises)))
