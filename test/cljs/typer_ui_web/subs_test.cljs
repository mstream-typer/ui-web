(ns typer-ui-web.subs-test
  (:require [clojure.test.check :as tc]
            [clojure.spec.test.alpha :as stest :include-macros true]
            [clojure.test :as test :include-macros true]
            [typer-ui-web.subs :as subs]))


(test/deftest next-row-index-test
  (let [result (first (stest/check `subs/next-row-index))]
    (test/is (and (-> result ::tc/ret :result)
                  (not (result :failure)))
             result)))


(test/deftest format-text-test
  (let [result (first (stest/check `subs/format-text))]
    (test/is (and (-> result ::tc/ret :result)
                  (not (result :failure)))
             result)))


(test/deftest words-fit-sheet-width?-test
  (let [result (first (stest/check `subs/words-fit-sheet-width?))]
    (test/is (and (-> result ::tc/ret :result)
                  (not (result :failure)))
             result))) 
