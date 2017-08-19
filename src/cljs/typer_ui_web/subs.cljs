(ns typer-ui-web.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [typer-ui-web.db :as db]
            [typer-ui-web.main-menu.subs :as main-menu-subs]
            [clojure.spec.alpha :as s] 
            [re-frame.core :as rf]))


(rf/reg-sub
 ::view
 (comp ::db/view
       ::db/ui))


(rf/reg-sub
 ::loader-visible
 (comp ::db/visible
       ::db/loader
       ::db/ui))


(rf/reg-sub
 ::modal-open
 :<- [::main-menu-subs/login-menu-visible]
 (fn [login-menu-visible _]
   login-menu-visible))
