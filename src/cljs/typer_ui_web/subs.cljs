(ns typer-ui-web.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [typer-ui-web.db :as db]
            [clojure.spec.alpha :as s] 
            [re-frame.core :as rf]))


(rf/reg-sub
 ::user-login
 (fn [db]
   (-> db
       (::db/user)
       (::db/login))))


(rf/reg-sub
 ::login-menu-visible
 (fn [db]
   (-> db
       (::db/ui)
       (::db/login-menu)
       (::db/visible))))


(rf/reg-sub
 ::login-menu-username
 (fn [db]
   (-> db
       (::db/ui)
       (::db/login-menu)
       (::db/username))))


(rf/reg-sub
 ::login-menu-password
 (fn [db]
   (-> db
       (::db/ui)
       (::db/login-menu)
       (::db/password))))


(rf/reg-sub
 ::view
 (fn [db]
   (-> db
       (::db/ui)
       (::db/view))))


(rf/reg-sub
 ::modal-opened
 :<- [::login-menu-visible]
 (fn [login-menu-visible _]
   login-menu-visible)) 















