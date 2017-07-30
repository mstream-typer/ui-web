(ns typer-ui-web.views-test
  (:require [clojure.test.check :as tc]
            [clojure.spec.test.alpha :as stest :include-macros true]
            [clojure.test :as test :include-macros true]
            [typer-ui-web.views :as views]))


(test/deftest character-class-test
  (let [result (first (stest/check `views/character-class))]
    (test/is (and (-> result ::tc/ret :result)
                  (not (result :failure)))
             result)))













