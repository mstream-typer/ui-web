(ns typer-ui-web.main-menu.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [typer-ui-web.main-menu.db :as db]
            [typer-ui-web.common.db :as common-db]
            [clojure.spec.alpha :as s]
            [re-frame.core :as rf]))


(rf/reg-sub
 ::user-dropdown-visible
 (comp ::db/visible
       ::db/user-dropdown
       ::db/ui
       ::db/main-menu))


(rf/reg-sub
 ::username
 (comp ::db/username
       ::db/user
       ::db/data
       ::db/main-menu))


(rf/reg-sub
 ::login-menu-visible
 (comp ::db/visible
       ::db/login-menu
       ::db/ui
       ::db/main-menu))


(rf/reg-sub
 ::login-menu-username
 (comp ::db/username
       ::db/login-menu
       ::db/ui
       ::db/main-menu))


(rf/reg-sub
 ::login-menu-password
 (comp ::db/password
       ::db/login-menu
       ::db/ui
       ::db/main-menu))


(rf/reg-sub
 ::login-menu-loader-visible
 (comp ::common-db/visible
       ::common-db/loader
       ::db/login-menu
       ::db/ui
       ::db/main-menu))
