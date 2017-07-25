(ns typer-ui-web.views-test
  (:require [clojure.test.check :as tc]
            [clojure.spec.test.alpha :as stest :include-macros true]
            [clojure.test :as test :include-macros true]
            [typer-ui-web.views :as views]))


(test/deftest words-fit-sheet-width?-test
  (let [result (first (stest/check `views/words-fit-sheet-width?))]
    (test/is (and (-> result ::tc/ret :result) (not (result :failure)))
             result)))


(test/deftest next-row-index-test
  (let [result (first (stest/check `views/next-row-index))]
    (test/is (and (-> result ::tc/ret :result) (not (result :failure)))
             result)))


(test/deftest format-text-test
  (let [result (first (stest/check `views/format-text))]
    (test/is (and (-> result ::tc/ret :result) (not (result :failure)))
             result)))


(test/deftest character-class-test
  (let [result (first (stest/check `views/character-class))]
    (test/is (and (-> result ::tc/ret :result) (not (result :failure)))
             result)))


(test/deftest top-margin-test
  (let [result (first (stest/check `views/top-margin))]
    (test/is (and (-> result ::tc/ret :result) (not (result :failure)))
             result)))


(test/deftest bottom-margin-test
  (let [result (first (stest/check `views/bottom-margin))]
    (test/is (and (-> result ::tc/ret :result) (not (result :failure)))
             result)))











