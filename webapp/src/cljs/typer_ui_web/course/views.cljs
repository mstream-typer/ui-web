(ns typer-ui-web.course.views
  (:require [typer-ui-web.common.core :refer [evt> <sub]]
            [typer-ui-web.course.db :as db]
            [typer-ui-web.common.db :as common-db]
            [typer-ui-web.events :as events]
            [typer-ui-web.course.events :as course-events]
            [typer-ui-web.course.subs :as subs]
            [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [clojure.string :as str]))


(defn exercise-card [id
                     name
                     desc
                     navigate-to-exercise-request-event]
  [:div.exercise-item.card
   [:div.content
    [:div.header name]
    [:div.description desc]]
   [:div.ui.bottom.attached.positive.button
    {:on-click #(evt> (conj navigate-to-exercise-request-event
                            id))}
    "Train"]])


(defn course-panel [navigate-to-exercise-request-event]
  (let [name (<sub [::subs/name])
        exercises (<sub [::subs/exercises])]
    [:div
     [:div name
      [:div#course-panel.ui.four.cards
       (for [{:keys [::common-db/id
                     ::db/name
                     ::db/description]} exercises]
         ^{:key id}
         [exercise-card
          id
          name
          description
          navigate-to-exercise-request-event])]]]))


(defn course-view [navigate-to-exercise-request-event]
  [course-panel
   [navigate-to-exercise-request-event]])








