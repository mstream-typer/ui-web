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


(defn course-menu [course-name
                   navigated-to-home-event]
  [:div#main-menu.ui.blue.large.labeled.icon.inverted.menu
   [:a.item
    {:on-click #(evt> navigated-to-home-event)}
    [:i.arrow.left.icon] "Back"]
   [:div.active.item
    [:i.folder.open.outline.icon] course-name]])


(defn exercise-card [id
                     name
                     desc
                     navigated-to-exercise-event]
  [:div.exercise-item.card
   {:id (str "exercise-" id)}
   [:div.content
    [:div.header name]
    [:div.description desc]]
   [:div.ui.bottom.attached.blue.button
    {:on-click #(evt> (conj navigated-to-exercise-event
                            id))}
    "Train"]])


(defn course-panel [navigated-to-exercise-event]
  (let [name (<sub [::subs/name])
        exercises (<sub [::subs/exercises])]
    [:div
     [:div#course-panel.ui.four.cards
      (for [{:keys [::common-db/id
                    ::db/name
                    ::db/description]} exercises]
        ^{:key id}
        [exercise-card
         id
         name
         description
         navigated-to-exercise-event])]]))


(defn course-view [navigated-to-home-event
                   navigated-to-exercise-event]
  (let [name (<sub [::subs/name])]
    [:div
     [course-menu
      name
      navigated-to-home-event]
     [course-panel
      navigated-to-exercise-event]]))
