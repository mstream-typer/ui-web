(ns typer-ui-web.course.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [typer-ui-web.course.db :as db]
            [typer-ui-web.common.db :as common-db]
            [clojure.spec.alpha :as s]
            [re-frame.core :as rf]))


(rf/reg-sub
 ::name
 (comp ::db/name
       ::db/data
       ::db/course))


(rf/reg-sub
 ::exercises
 (comp ::db/exercises
       ::db/data
       ::db/course))


(rf/reg-sub
 ::loading?
 (comp ::common-db/visible
       ::common-db/loader
       ::db/ui
       ::db/course))
