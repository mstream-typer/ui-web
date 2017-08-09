(ns typer-ui-web.views
  (:require [typer-ui-web.common :refer [evt> <sub]]
            [typer-ui-web.db :as db]
            [typer-ui-web.events :as events]
            [typer-ui-web.subs :as subs]
            [typer-ui-web.exercise.views :as exercise-views]
            [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]))
  
 
(defn main-menu []
  [:div
   [:div.ui.large.menu
    [:div.right.menu
     [:a.item
      {:on-click #(evt> [::events/login-menu-button-pressed])}
      "Sign In"]]]])


(defn login-menu []
  (let [visible (<sub [::subs/login-menu-visible])
        username (<sub [::subs/login-menu-username])
        password (<sub [::subs/login-menu-password])]
    [:div.ui.standard.modal.transition
     {:class (if visible
               "visible active"
               "hidden")}
     [:div.header "Login"]
     [:div.content
      [:form.ui.form
       [:div.field
        [:label "Username"]
        [:input#username
         {:name "username"
          :type "text"
          :value username 
          :on-change #(evt> [::events/login-menu-username-changed
                             (-> %
                                 .-target
                                 .-value)])}]]
       [:div.field
        [:label "Password"]
        [:input#password
         {:name "password"
          :type "password"
          :value password
          :on-change #(evt> [::events/login-menu-password-changed 
                             (-> %
                                 .-target
                                 .-value)])}]]
       [:div.actions
        [:div.ui.black.deny.button
         {:on-click #(evt> [::events/cancel-login-menu-button-pressed])}
         "Cancel"]
        [:div.ui.positive.right.labeled.icon.button
         {:on-click #(evt> [::events/cancel-login-menu-button-pressed])}
         "Sign In"
         [:i.sign.in.icon]]]]]])) 


(defn dimmer []
  (let [loader-visible? (<sub [::subs/loader-visible])
        modal-open? (<sub [::subs/modal-open])]
    [:div#dimmer.ui.dimmer.modals.page.transition
     {:class (if (or loader-visible?
                     modal-open?)
               "visible active"
               "hidden")}
     [:div.ui.loader
      {:class (when (not loader-visible?)
                "hidden")}] 
     [login-menu]]))


(defn home-view []
  [:div
   [main-menu] 
   [:button.ui.button
    {:on-click #(evt> [::events/navigated-to-exercise])}
    "Start"]])


(defn view []
  (let [view (<sub [::subs/view])]
    (case view
      ::db/home [home-view]
      ::db/exercise [exercise-views/exercise-view])))


(defn main-panel []
  [:div#main-panel
   [dimmer]
   [view]])
