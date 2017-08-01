(ns typer-ui-web.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [typer-ui-web.subs]
            [typer-ui-web.events]
            [typer-ui-web.views]
            [clojure.test.check :as tc]
            [clojure.spec.test.alpha :as stest :include-macros true]
            [clojure.test :as test :include-macros true]))


(test/deftest spec-tests
  (let [result (stest/check)
        failures (filter #(-> % ::tc/ret :result not) result)]
    (test/is (empty? failures))))


(doo-tests)
