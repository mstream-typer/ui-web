(ns typer-ui-web.main-menu.db
  (:require [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [typer-ui-web.common.db :as common-db]))


(s/def ::password
  string?)


(s/def ::username
  string?)


(s/def ::user
  (s/keys :req [::password
                ::username]))


(s/def ::login-menu
  (s/keys :req [::common-db/loader
                ::common-db/visible
                ::username
                ::password]))


(s/def ::user-dropdown
  (s/keys :req [::common-db/visible]))


(s/def ::data
  (s/keys :req [::user]))


(s/def ::ui
  (s/keys :req [::user-dropdown
                ::login-menu]))


(s/def ::main-menu
  (s/keys :req [::ui]))


(def default-db
  {::main-menu {::data {::user {::username ""
                                ::password ""}}
                ::ui {::user-dropdown {::common-db/visible false}
                      ::login-menu {::common-db/loader {::common-db/visible false}
                                    ::common-db/visible false
                                    ::username ""
                                    ::password ""}}}})

                       
