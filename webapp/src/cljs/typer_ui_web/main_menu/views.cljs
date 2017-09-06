(ns typer-ui-web.main-menu.views
  (:require [typer-ui-web.common.core :refer [evt> <sub]]
            [typer-ui-web.main-menu.db :as db]
            [typer-ui-web.main-menu.events :as events]
            [typer-ui-web.main-menu.subs :as subs]
            [re-frame.core :as rf]))


(defn login-menu []
  (let [visible (<sub [::subs/login-menu-visible])
        username (<sub [::subs/login-menu-username])
        password (<sub [::subs/login-menu-password])
        loading? (<sub [::subs/login-menu-loader-visible])]
    [:div#login-menu.ui.standard.modal.transition
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
          ;TODO: get rid of dependencies on the db namespace
          :on-click #(evt> [::events/user-sign-in-requested
                            {::db/username username
                             ::db/password password}])}
         "Sign In"
         [:i.sign.in.icon]]]]]]))


(defn sign-in-menu-item []
  (let [username (<sub [::subs/username])
        signed-in? (seq username)]
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
        "Sign In"]])))


(defn main-menu []
    [:div#main-menu
     [:div.ui.large.menu
      [sign-in-menu-item]]])
