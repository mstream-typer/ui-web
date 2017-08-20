(require '[clojure.test :as test])
(require '[etaoin.api :as eta])
(require '[etaoin.keys :as keys])


(def driver (eta/phantom))


(Given #"^typer service responds with course exercises$"
       [] nil)

       
(When #"^users enter the home page$"
      []
      (doto driver
        (eta/go "http://localhost")
        (eta/wait-visible {:css "#main-panel"})
        (eta/wait-visible {:css "#course-panel"})))
      

(Then #"^user should see following exercises$"
      []
      (let [exercise-items (eta/query-all driver
                                          {:css "#course-panel .exercise-item"})]
        (test/is (= 5 (count exercise-items)))))



