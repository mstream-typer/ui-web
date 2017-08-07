(ns typer-ui-web.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [typer-ui-web.subs]
            [typer-ui-web.events]
            [typer-ui-web.views]
            [typer-ui-web.exercise.subs]
            [typer-ui-web.exercise.events]
            [typer-ui-web.exercise.views]
            [clojure.pprint :as pp]
            [clojure.test.check :as tc]
            [clojure.test.check.properties :as tcp]
            [clojure.spec.alpha :as s] 
            [clojure.spec.test.alpha :as stest :include-macros true]
            [clojure.test :as test :include-macros true]))


(defn results->failures [results]
  (->> results
       (filter (comp not true? :result ::tc/ret))
       (map #(let [{failure :failure
                    function :sym
                    {{{ex-info ::tcp/error} :result-data} :shrunk} ::tc/ret} %
                   ex-data (ex-data ex-info)
                   values (::stest/val ex-data)]
               (cond
                 failure {:function function
                          :failure failure}
                 (not ex-data) {:function function
                                :error ex-info}
                 :else {:function function
                        :parameters (:args values)
                        :return (:ret values)
                        :problems (::s/problems ex-data)})))))


(test/deftest spec-tests
  (let [results (stest/check [`typer-ui-web.subs
                              `typer-ui-web.events
                              `typer-ui-web.views
                              `typer-ui-web.exercise.subs
                              `typer-ui-web.exercise.events
                              `typer-ui-web.exercise.views]
                             {:clojure.test.check/opts {:num-tests 50}})
        failures (results->failures results)
        failures-cnt (count failures)]
;    (pp/pprint results) 
    (when (not (empty? failures)) (do (pp/pprint "===>")
                                      (pp/pprint failures)
                                      (pp/pprint (str "there are "
                                                      failures-cnt
                                                      " test failures"))
                                      (pp/pprint "<===")))
    (test/is (empty? (map :function failures)))))
 

(doo-tests)

