(ns typer-ui-web.events-test
  (:require [clojure.test.check :as tc]
            [clojure.spec.test.alpha :as stest :include-macros true]
            [clojure.test :as test :include-macros true]
            [typer-ui-web.events :as events]))


(test/deftest character-typed-test
  (let [result (first (stest/check `events/character-typed))]
    (test/is (and (-> result ::tc/ret :result)
                  (not (result :failure)))
             result)))


(test/deftest login-menu-button-pressed-test 
  (let [result (first (stest/check `events/login-menu-button-pressed))]
    (test/is (and (-> result ::tc/ret :result)
                  (not (result :failure)))
             result)))


(test/deftest cancel-login-menu-button-pressed-test 
  (let [result (first (stest/check `events/cancel-login-menu-button-pressed))]
    (test/is (and (-> result ::tc/ret :result)
                  (not (result :failure)))
             result)))


(test/deftest login-menu-username-changed-test
  (let [result (first (stest/check `events/login-menu-username-changed))]
    (test/is (and (-> result ::tc/ret :result)
                  (not (result :failure)))
             result)))


(test/deftest login-menu-password-changed-test
  (let [result (first (stest/check `events/login-menu-password-changed))]
    (test/is (and (-> result ::tc/ret :result)
                  (not (result :failure)))
             result)))
