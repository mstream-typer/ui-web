(ns typer-ui-web.views
  (:require [typer-ui-web.common.core :refer [evt> <sub]]
            [typer-ui-web.db :as db]
            [typer-ui-web.events :as events]
            [typer-ui-web.subs :as subs]
            [typer-ui-web.course.views :as course-views]
            [typer-ui-web.exercise.views :as exercise-views]
            [typer-ui-web.main-menu.views :as main-menu-views]
            [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]))


(defn dimmer []
  (let [loader-visible? (<sub [::subs/loading?])
        modal-open? (<sub [::subs/modal-open])]
    [:div#dimmer.ui.dimmer.modals.page.transition
     {:class (if (or loader-visible?
                     modal-open?)
               "visible active"
               "hidden")}
     [:div.ui.loader
      {:class (when (not loader-visible?)
                "hidden")}] 
     [main-menu-views/login-menu]]))

 
(defn home-view []
  [:div
   [main-menu-views/main-menu]
   [:div.course.ui.button
    {:on-click #(evt> [::events/navigated-to-course 1])}
    "Course"]])


(defn view []
  (let [view (<sub [::subs/view])]
    (case view
      ::db/home [home-view]
      ::db/course [course-views/course-view
                   [::events/navigate-to-exercise-requested]]
      ::db/exercise [exercise-views/exercise-view
                     [::events/navigate-to-home-requested]])))

 
(defn main-panel []
  [:div#main-panel
   [dimmer]
   [view]])
