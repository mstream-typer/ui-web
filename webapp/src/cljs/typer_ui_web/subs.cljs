(ns typer-ui-web.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [typer-ui-web.db :as db]
            [typer-ui-web.course.subs :as course-subs]
            [typer-ui-web.exercise.subs :as exercise-subs]
            [typer-ui-web.main-menu.subs :as main-menu-subs]
            [clojure.spec.alpha :as s] 
            [re-frame.core :as rf]))


(rf/reg-sub
 ::view
 (comp ::db/view
       ::db/ui))


(defn modal-open [login-menu-visible _]
  login-menu-visible)

(rf/reg-sub
 ::modal-open
 :<- [::main-menu-subs/login-menu-visible]
 modal-open)


(defn loading? [loadings _]
  (some true?
        loadings)
  false)

(rf/reg-sub
 ::loading?
 :<- [::course-subs/loading?]
 :<- [::exercise-subs/loading?]
 loading?)


 
