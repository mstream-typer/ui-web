(ns typer-ui-web.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [typer-ui-web.subs]
            [typer-ui-web.events]
            [typer-ui-web.views]
            [typer-ui-web.course.subs]
            [typer-ui-web.course.events]
            [typer-ui-web.course.views]
            [typer-ui-web.exercise.subs]
            [typer-ui-web.exercise.events]
            [typer-ui-web.exercise.views]
            [typer-ui-web.home.subs]
            [typer-ui-web.home.events]
            [typer-ui-web.home.views]
            [typer-ui-web.main-menu.subs]
            [typer-ui-web.main-menu.events]
            [typer-ui-web.main-menu.views]
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
                              `typer-ui-web.course.subs
                              `typer-ui-web.course.events
                              `typer-ui-web.course.views
                              `typer-ui-web.exercise.subs
                              `typer-ui-web.exercise.events
                              `typer-ui-web.exercise.views
                              `typer-ui-web.home.subs
                              `typer-ui-web.home.events
                              `typer-ui-web.home.views
                              `typer-ui-web.main-menu.subs
                              `typer-ui-web.main-menu.events
                              `typer-ui-web.main-menu.views]
                             {:clojure.test.check/opts {:num-tests 50}})
        failures (results->failures results)
        failures-cnt (count failures)]
    (when (seq failures)
      (pp/pprint "===>")
      (pp/pprint failures)
      (pp/pprint (str "there are "
                      failures-cnt
                      " test failures"))
      (pp/pprint "<==="))
    (test/is (empty? (map :function failures)))))


(doo-tests)
