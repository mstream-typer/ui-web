(ns typer-ui-web.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [typer-ui-web.db :as db]
            [clojure.spec.alpha :as s] 
            [re-frame.core :as rf]))


(rf/reg-sub
 ::user-login
 #(-> %
      (::db/user)
      (::db/login)))


(rf/reg-sub
 ::login-menu-visible
 #(-> %
      (::db/ui)
      (::db/login-menu)
      (::db/visible)))


(rf/reg-sub
 ::login-menu-username
 #(-> %
      (::db/ui)
      (::db/login-menu)
      (::db/username)))


(rf/reg-sub
 ::login-menu-password
 #(-> %
      (::db/ui)
      (::db/login-menu)
      (::db/password)))


(rf/reg-sub
 ::view
 #(-> %
      (::db/ui)
      (::db/view)))


(rf/reg-sub
 ::loader-visible
 #(-> %
      (::db/ui)
      (::db/loader)
      (::db/visible)))


(rf/reg-sub
 ::modal-open
 :<- [::login-menu-visible]
 (fn [login-menu-visible _]
   login-menu-visible))
