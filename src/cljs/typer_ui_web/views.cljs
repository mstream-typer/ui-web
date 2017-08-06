(ns typer-ui-web.views
  (:require [typer-ui-web.db :as db]
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
      {:on-click #(rf/dispatch [::events/login-menu-button-pressed])}
      "Sign In"]]]])


(defn login-menu []
  (let [active @(rf/subscribe [::subs/login-menu-visible])
        username @(rf/subscribe [::subs/login-menu-username])
        password @(rf/subscribe [::subs/login-menu-password])]
    [:div.ui.standard.modal.transition
     {:class (if active
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
          :on-change #(rf/dispatch [::events/login-menu-username-changed
                                    (-> %
                                        .-target
                                        .-value)])}]]
       [:div.field
        [:label "Password"]
        [:input#password
         {:name "password"
          :type "password"
          :value password
          :on-change #(rf/dispatch [::events/login-menu-password-changed 
                                    (-> %
                                        .-target
                                        .-value)])}]]
     [:div.actions
      [:div.ui.black.deny.button
       {:on-click #(rf/dispatch [::events/cancel-login-menu-button-pressed])}
       "Cancel"]
      [:div.ui.positive.right.labeled.icon.button
       {:on-click #(rf/dispatch [::events/cancel-login-menu-button-pressed])}
       "Sign In"
       [:i.sign.in.icon]]]]]])) 


(defn dimmer []
  (let [active @(rf/subscribe [::subs/modal-opened])]
    [:div#dimmer.ui.dimmer.modals.page.transition
     {:class (if active
               "visible active"
               "hidden")}
     [login-menu]]))


(defn home-view []
  [:div
   [main-menu]
   [:button.ui.button
    {:on-click #(rf/dispatch [::events/navigated-to-exercise])}
    "Start"]])


(defn view []
  (let [view @(rf/subscribe [::subs/view])]
    (case view
      ::db/home [home-view]
      ::db/exercise [exercise-views/exercise-view])))


(defn main-panel []
  [:div#main-panel
   [dimmer]
   [view]])
