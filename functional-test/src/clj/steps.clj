(require '[clojure.test :as test])
(require '[clojure.string :as str])
(require '[etaoin.api :as eta])
(require '[etaoin.keys :as keys])
(require '[org.httpkit.client :as http])
(require '[clojure.data.json :as json])


(def wait-opts {:timeout 2
                :interval 0.2})


(defn wait-visible [driver css]
  (eta/wait-visible driver
                    css
                    wait-opts))


(defn wait-invisible [driver css]
  (eta/wait-invisible driver
                      css
                      wait-opts))


(def courses-api-query-fmt
  "^\\{courses\\{id name description\\}\\}$")


(def course-api-query-fmt
  "^\\{course\\(id:\\w+\\)\\{name exercises\\{id name description\\}\\}\\}$")


(def exercise-api-query-fmt
  "^\\{exercise\\(id:\\w+\\)\\{time text\\}\\}$")


(def wiremock-api "http://localhost:8080/__admin")


(def wiremock-mappings (str wiremock-api
                            "/mappings"))


(def postmortem-opts {:dir "/tmp"})


(def driver (eta/boot-driver :phantom
                             {:args ["--web-security=false"]}))


(eta/set-window-size driver 800 600)


(eta/use-css driver)


(defn reset-stubs []
  (let [{:keys [status]} @(http/delete wiremock-mappings)]))


(defn stub [request response]
  (let [body {:body (json/write-str {"request" request
                                     "response" response})}
        {:keys [status body]} @(http/post wiremock-mappings
                                          body)]
    (when (not= 201 status)
      (throw (Exception. (str "error while stubbing: "
                              body))))))


(Before
 []
 (reset-stubs))


(Given
 #"^typer service responds with following courses$"
 [courses-data]
 (stub {:method "GET"
        :urlPath "/graphql"
        :queryParameters {:query {:matches courses-api-query-fmt}}}
       {:status 200
        :jsonBody {:data {:courses (table->rows courses-data)}}}))


(Given
 #"^typer service responds with following course '(\w+)' exercises$"
 [course-id exercises-data]
 (stub {:method "GET"
        :urlPath "/graphql"
        :queryParameters {:query {:matches (format course-api-query-fmt
                                                   course-id)}}}
       {:status 200
        :jsonBody {:data {:course {:name course-id
                                   :exercises (table->rows exercises-data)}}}}))


(Given
 #"^typer service responds with following exercise '(\w+)' details$"
 [exercise-id exercise-data]
 (let [{:keys [time text]} (first (table->rows exercise-data))]
   (stub {:method "GET"
          :urlPath "/graphql"
          :queryParameters {:query {:matches (format exercise-api-query-fmt
                                                     exercise-id)}}}
         {:status 200
          :jsonBody {:data {:exercise {:time time
                                       :text text}}}})))


(When
 #"^user enters the home page$"
 []
 (eta/with-postmortem
   driver
   postmortem-opts
   (doto driver
     (eta/go "http://localhost"))))


(When
 #"^user navigates to the course called '(.*)'$"
 [course-name]
 (eta/with-postmortem
   driver
   postmortem-opts
   (doto driver
     (wait-invisible "#dimmer .loader")
     (wait-visible "#courses-panel"))
   (->> (eta/query-all driver ".course-item")
        (filter #(str/includes? (eta/get-element-text-el driver
                                                         %)
                                course-name))
        (first)
        (#(eta/get-element-attr-el driver % :id))
        (#(eta/click driver (str "#" % " .button"))))))


(When
 #"^user navigates to the exercise called '(.*)'$"
 [exercise-name]
 (eta/with-postmortem
   driver
   postmortem-opts
   (doto driver
     (wait-invisible "#dimmer .loader")
     (wait-visible "#course-panel"))
   (->> (eta/query-all driver ".exercise-item")
        (filter #(str/includes? (eta/get-element-text-el driver
                                                         %)
                                exercise-name))
        (first)
        (#(eta/get-element-attr-el driver % :id))
        (#(eta/click driver (str "#" % " .button"))))))


(Then
 #"^user should see following courses$"
 [courses-data]
 (eta/with-postmortem
   driver
   postmortem-opts
   (doto driver
     (wait-invisible "#dimmer .loader")
     (wait-visible "#courses-panel"))
   (let [expected-courses (set (table->rows courses-data))
         exercise-texts (map (partial eta/get-element-text-el
                                      driver)
                             (eta/query-all driver ".course-item"))]
     (test/is (= (count expected-courses)
                 (count exercise-texts)))
     (test/is (every? (fn [{:keys [name
                                   description]}]
                        (some (fn [exercise-text]
                                (and (str/includes? exercise-text
                                                    name)
                                     (str/includes? exercise-text
                                                    description)
                                     (str/includes? exercise-text
                                                    "Start")))
                              exercise-texts))
                      expected-courses)))))


(Then
 #"^user should see following exercises$"
 [exercises-data]
 (eta/with-postmortem
   driver
   postmortem-opts
   (doto driver
     (wait-invisible "#dimmer .loader")
     (wait-visible "#course-panel"))
   (let [expected-exercises (set (table->rows exercises-data))
         exercise-texts (map (partial eta/get-element-text-el
                                      driver)
                             (eta/query-all driver ".exercise-item"))]
     (test/is (= (count expected-exercises)
                 (count exercise-texts)))
     (test/is (every? (fn [{:keys [name
                                   description]}]
                        (some (fn [exercise-text]
                                (and (str/includes? exercise-text
                                                    name)
                                     (str/includes? exercise-text
                                                    description)
                                     (str/includes? exercise-text
                                                    "Train")))
                              exercise-texts))
                      expected-exercises)))))


(Then
 #"^user should see a timer displaying '(.+)'$"
 [timer-value]
 (eta/with-postmortem
   driver
   postmortem-opts
   (doto driver
     (wait-invisible "#dimmer .loader")
     (wait-visible "#exercise"))
   (let [timer-text (eta/get-element-text driver "#timer")]
     (test/is (str/includes? timer-text
                             timer-value)))))


(Then
 #"^user should see following text hints$"
 [lines-data]
 (eta/with-postmortem
   driver
   postmortem-opts
   (doto driver
     (wait-invisible "#dimmer .loader")
     (wait-visible "#exercise"))
   (let [expected-lines (map #(get %
                                   :line
                                   "")
                             (table->rows lines-data))
         actual-lines (map (partial eta/get-element-text-el
                                    driver)
                           (eta/query-all driver
                                          "#exercise .line"))]
     (test/is (= actual-lines
                 expected-lines)))))


(When
 #"^user types '([^']+)'$"
 [text]
 (eta/with-postmortem
   driver
   postmortem-opts
   (doto driver
     (wait-invisible "#dimmer .loader")
     (wait-visible "#exercise")
     (eta/fill "#exercise" text))))
