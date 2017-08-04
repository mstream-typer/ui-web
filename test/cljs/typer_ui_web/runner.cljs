(ns typer-ui-web.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [typer-ui-web.subs]
            [typer-ui-web.events]
            [typer-ui-web.views]
            [clojure.test.check :as tc]
            [clojure.test.check.properties :as tcp]
            [clojure.spec.alpha :as s] 
            [clojure.spec.test.alpha :as stest :include-macros true]
            [clojure.test :as test :include-macros true]))


(defn results->failures [results]
  (->> results
       (filter (comp not :result ::tc/ret))
       (map #(let [{function :sym
                    {{{ex-info ::tcp/error} :result-data} :shrunk} ::tc/ret} %
                   ex-data (ex-data ex-info)
                   values (::stest/val ex-data)]
               {:function function
                :parameters (:args values)
                :return (:ret values)
                :problems (::s/problems ex-data)}))))


(test/deftest spec-tests
  (let [results (stest/check [`typer-ui-web.subs
                              `typer-ui-web.events
                              `typer-ui-web.views]
                             {:clojure.test.check/opts {:num-tests 100}})
        failures (results->failures results)]
    (test/is (empty? (map :function failures) failures))))


(doo-tests)

