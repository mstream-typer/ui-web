(ns typer-ui-web.course.views
  (:require [typer-ui-web.common :refer [evt> <sub]]
            [typer-ui-web.course.db :as course-db]
            [typer-ui-web.events :as events]
            [typer-ui-web.course.events :as course-events]
            [typer-ui-web.course.subs :as course-subs]
            [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [clojure.string :as str]))


(defn exercise-card [id title desc]
  [:div.card
   [:div.content
    [:div.header title]
    [:div.description desc]]
   [:div.ui.bottom.attached.positive.button
    {:on-click #(evt> [::events/navigated-to-exercise id])}
    "Train"]])


(defn course-panel []
  (let [exercises (<sub [::course-subs/exercises])] 
  [:div.ui.four.cards
   (for [exercise exercises]
     ^{:key (::course-db/id exercise)}
     [exercise-card
      (::course-db/id exercise)
      (::course-db/title exercise)
      (::course-db/description exercise)])]))
