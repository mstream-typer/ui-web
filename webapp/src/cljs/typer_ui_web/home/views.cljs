(ns typer-ui-web.home.views
  (:require [typer-ui-web.common.core :refer [evt> <sub]]
            [typer-ui-web.common.db :as common-db]
            [typer-ui-web.home.db :as db]
            [typer-ui-web.home.events :as events]
            [typer-ui-web.home.subs :as subs]
            [re-frame.core :as rf]))


(defn course-card [id
                   name
                   desc
                   navigate-to-exercise-request-event]
   [:div.course-item.card
    {:id (str "course-" id)}
    [:div.image
     [:img {:src "http://via.placeholder.com/300x300"}]]
    [:div.content
     [:div.header name]
     [:div.description desc]]
    [:div.ui.bottom.attached.blue.button
     {:on-click #(evt> (conj navigate-to-exercise-request-event
                             id))}
     "Start"]])


(defn courses-panel [navigate-to-exercise-request-event]
  (let [courses (<sub [::subs/courses])]
    [:div#courses-panel
     [:div.ui.cards
      (for [{:keys [::common-db/id
                    ::db/name
                    ::db/description]} courses]
        ^{:key id}
        [course-card
         id
         name
         description
         navigate-to-exercise-request-event])]]))


(defn home-view [main-menu
                 navigate-to-exercise-request-event]
  [:div
   [main-menu]
   [courses-panel
    navigate-to-exercise-request-event]])
