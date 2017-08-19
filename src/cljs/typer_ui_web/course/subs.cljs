(ns typer-ui-web.course.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [typer-ui-web.course.db :as db]
            [clojure.spec.alpha :as s] 
            [re-frame.core :as rf]))


(rf/reg-sub
 ::exercises
 (comp ::db/exercises
       ::db/data
       ::db/course))
