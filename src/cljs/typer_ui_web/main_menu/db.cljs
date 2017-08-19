(ns typer-ui-web.main-menu.db
  (:require [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]))


(s/def ::visible
  boolean?)


(s/def ::password
  string?)


(s/def ::username
  string?)


(s/def ::user
  (s/keys :req [::password
                ::username]))


(s/def ::loader
  (s/keys :req [::visible]))


(s/def ::login-menu
  (s/keys :req [::loader
                ::visible
                ::username
                ::password]))


(s/def ::user-dropdown
  (s/keys :req [::visible]))


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
                ::ui {::user-dropdown {::visible false}
                      ::login-menu {::loader {::visible false}
                                    ::visible false
                                    ::username ""
                                    ::password ""}}}})



                       
