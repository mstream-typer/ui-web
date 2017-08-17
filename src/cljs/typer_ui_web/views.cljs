(ns typer-ui-web.views
  (:require [typer-ui-web.common :refer [evt> <sub]]
            [typer-ui-web.db :as db]
            [typer-ui-web.events :as events]
            [typer-ui-web.subs :as subs]
            [typer-ui-web.course.views :as course-views]
            [typer-ui-web.exercise.views :as exercise-views]
            [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]))
  

(defn sign-in-menu-item []
  (let [username (<sub [::subs/username])
        signed-in? (not (empty? username))]
    (if signed-in?
      (let [dropdown-open? (<sub [::subs/user-dropdown-visible])]
        [:div.ui.right.dropdown.item
         {:class (when dropdown-open? "visible active")
          :on-click #(evt> [::events/user-dropdown-switched])}
         username
         [:i.dropdown.icon]
         [:div.menu.transition
          {:class (if dropdown-open?
                    "visible"
                    "hidden")}
          [:div.item "View profile"]
          [:div.divider]
          [:div.item
           {:on-click #(evt> [::events/user-signed-out])}
           "Sign out"]]])
      [:div.right.menu
       [:a.item
        {:on-click #(evt> [::events/login-menu-button-pressed])}
        "Sing In"]])))



(defn main-menu []
    [:div
     [:div.ui.large.menu
      [sign-in-menu-item]]])
      

(defn login-menu []
  (let [visible (<sub [::subs/login-menu-visible])
        username (<sub [::subs/login-menu-username])
        password (<sub [::subs/login-menu-password])
        loading? (<sub [::subs/login-menu-loader-visible])]
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
         {:class (when loading?
                   "disabled")
          :on-click #(evt> [::events/cancel-login-menu-button-pressed])}
         "Cancel"]
        [:div.ui.positive.right.labeled.icon.button
         {:class (when loading?
                   "loading")
          :on-click #(evt> [::events/user-signed-in {::db/username username
                                                     ::db/password password}])}
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
   [course-views/course-panel]])


(defn view []
  (let [view (<sub [::subs/view])]
    (case view
      ::db/home [home-view]
      ::db/exercise [exercise-views/exercise-view])))

 
(defn main-panel []
  [:div#main-panel
   [dimmer]
   [view]])
