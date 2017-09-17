(ns typer-ui-web.exercise.events
  (:require [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [ajax.core :as ajax]
            [cemerick.url :as url]
            [cuerdas.core :as str]
            [day8.re-frame.http-fx]
            [typer-ui-web.common.events :as common-events]
            [typer-ui-web.config :as config]
            [typer-ui-web.db :as core-db]
            [typer-ui-web.exercise.db :as db]
            [typer-ui-web.common.db :as common-db]))


(def exercise-query-fmt
  "{exercise(id:%s){time text}}")


(def response-format
  (ajax/json-response-format {:keywords? true}))


(s/def ::time
  int?)


(s/def ::text
  string?)


(s/def ::exercise
  (s/keys :req-un [::time ::text]))


(s/def ::data
  (s/keys :req-un [::exercise]))


(s/def ::exercise-response
  (s/keys :req-un [::data]))


(s/def ::exercise-loading-succeed-event
  (s/tuple keyword?
           ::exercise-response))


(s/def ::exercise-loading-requested-event
  (s/tuple keyword?
           string?))


(s/def ::character-typed-event
  (s/tuple #{::character-typed}
           (s/with-gen char? (fn [] gen/char-ascii))))


(s/def ::loader-shows-up
  (comp ::common-db/visible
        ::common-db/loader
        ::db/ui
        ::db/exercise
        :db))


(s/def ::loader-hides
  (comp not
        ::common-db/visible
        ::common-db/loader
        ::db/ui
        ::db/exercise
        :db))


(s/def ::loader-hides-when-not-dispatching
  #(let [loader-visible? (-> %
                             :db
                             ::db/exercise
                             ::db/ui
                             ::common-db/loader
                             ::common-db/visible)
         dispatching? (:dispatch %)]
     (if-not dispatching?
       (not loader-visible?)
       true)))


(s/def ::starts-exercise
  (comp ::db/started
        ::db/data
        ::db/exercise
        :db))


(s/def ::exercise-timer-counts-down
  #(let [in-started (-> %
                        (:args)
                        (:cofx)
                        (:db)
                        (::db/exercise)
                        (::db/data)
                        (::db/started))
         in-finished (-> %
                         (:args)
                         (:cofx)
                         (:db)
                         (::db/exercise)
                         (::db/data)
                         (::db/finished))
         in-timer (-> %
                      (:args)
                      (:cofx)
                      (:db)
                      (::db/exercise)
                      (::db/data)
                      (::db/timer)
                      (::db/current))
         out-timer (-> %
                       (:ret)
                       (:db)
                       (::db/exercise)
                       (::db/data)
                       (::db/timer)
                       (::db/current))]
     (if (and in-started
              (not in-finished)
              (pos? in-timer))
         (= out-timer (dec in-timer))
         true)))


(s/def ::exercise-timer-does-not-go-below-zero
  (comp not
        neg?
        ::db/current
        ::db/timer
        ::db/data
        ::db/exercise
        ::db))


(s/def ::finishes-exercise-when-reaches-zero
  #(let [in-timer (-> %
                      (:args)
                      (:cofx)
                      (:db)
                      (::db/exercise)
                      (::db/data)
                      (::db/timer)
                      (::db/current))
         in-started? (-> %
                         (:args)
                         (:cofx)
                         (:db)
                         (::db/exercise)
                         (::db/data)
                         (::db/started))
         in-finished? (-> %
                          (:args)
                          (:cofx)
                          (:db)
                          (::db/exercise)
                          (::db/data)
                          (::db/finished))
         out-finished (-> %
                          (:ret)
                          (:db)
                          (::db/exercise)
                          (::db/data)
                          (::db/finished))
         out-summary-modal-visible (-> %
                                       (:ret)
                                       (:db)
                                       (::db/exercise)
                                       (::db/ui)
                                       (::db/summary-modal)
                                       (::common-db/visible))]
     (if (and in-started?
              (not in-finished?)
              (zero? in-timer))
       (and out-finished
            out-summary-modal-visible)
       true)))


(s/def ::tick-ignored-if-exercise-not-started
  #(let [in-started? (-> %
                         (:args)
                         (:cofx)
                         (:db)
                         (::db/exercise)
                         (::db/data)
                         (::db/started))
         in-timer (-> %
                      (:args)
                      (:cofx)
                      (:db)
                      (::db/exercise)
                      (::db/data)
                      (::db/timer)
                      (::db/current))
         out-timer (-> %
                       (:ret)
                       (:db)
                       (::db/exercise)
                       (::db/data)
                       (::db/timer)
                       (::db/current))]
     (if-not in-started?
       (= out-timer in-timer)
       true)))


(s/def ::tick-ignored-if-exercise-finished
  #(let [in-finished? (-> %
                          (:args)
                          (:cofx)
                          (:db)
                          (::db/exercise)
                          (::db/data)
                          (::db/finished))
         in-timer (-> %
                      (:args)
                      (:cofx)
                      (:db)
                      (::db/exercise)
                      (::db/data)
                      (::db/timer)
                      (::db/current))
         out-timer (-> %
                       (:ret)
                       (:db)
                       (::db/exercise)
                       (::db/data)
                       (::db/timer)
                       (::db/current))]
     (if in-finished?
       (= out-timer in-timer)
       true)))


(s/def ::exercise-state-resets
  #(let [in-exercise (-> %
                         (:args)
                         (:cofx)
                         (:db)
                         (::db/exercise))
         out-exercise (-> %
                          (:ret)
                          (:db)
                          (::db/exercise))
         default-exercise (::db/exercise db/default-db)]
     (and (= (-> out-exercise
                 ::db/ui
                 ::db/summary-modal)
             (-> default-exercise
                 ::db/ui
                 ::db/summary-modal))
          (= (-> out-exercise
                 ::db/ui
                 ::db/sheet)
             (-> in-exercise
                 ::db/ui
                 ::db/sheet))
          (= (-> out-exercise
                 ::db/data
                 ::db/started)
             (-> default-exercise
                 ::db/data
                 ::db/started))
          (= (-> out-exercise
                 ::db/data
                 ::db/finished)
             (-> default-exercise
                 ::db/data
                 ::db/finished))
          (= (-> out-exercise
                 ::db/data
                 ::db/timer
                 ::db/initial)
             (-> in-exercise
                 ::db/data
                 ::db/timer
                 ::db/initial))
          (= (-> out-exercise
                 ::db/data
                 ::db/timer
                 ::db/current)
             (-> in-exercise
                 ::db/data
                 ::db/timer
                 ::db/initial))
          (= (-> out-exercise
                 ::db/data
                 ::db/text
                 ::db/expected)
             (-> in-exercise
                 ::db/data
                 ::db/text
                 ::db/expected))
          (= (-> out-exercise
                 ::db/data
                 ::db/text
                 ::db/current)
             (-> default-exercise
                 ::db/data
                 ::db/text
                 ::db/current)))))


(s/def ::finishes-exercise-when-whole-text-matches
  #(let [ch (-> %
                (:args)
                (:event)
                (second))
         in-text-actual (-> %
                            (:args)
                            (:cofx)
                            (:db)
                            (::db/exercise)
                            (::db/data)
                            (::db/text)
                            (::db/actual))
         in-text-expected (-> %
                              (:args)
                              (:cofx)
                              (:db)
                              (::db/exercise)
                              (::db/data)
                              (::db/text)
                              (::db/expected))
         out-exercise-finished (-> %
                                   (:ret)
                                   (:db)
                                   (::db/exercise)
                                   (::db/finished))
         out-summary-modal-visible (-> %
                                       (:ret)
                                       (:db)
                                       (::db/exercise)
                                       (::db/ui)
                                       (::db/summary-modal)
                                       (::common-db/visible))]
     (if (= in-text-expected
            (conj in-text-actual ch))
       (and out-exercise-finished
            out-summary-modal-visible)
       true)))


(s/def ::ignores-typing-after-exercise-is-finished
  #(let [in-text-actual (-> %
                            (:args)
                            (:cofx)
                            (:db)
                            (::db/exercise)
                            (::db/data)
                            (::db/text)
                            (::db/actual))
         in-finished (-> %
                         (:args)
                         (:cofx)
                         (:db)
                         (::db/exercise)
                         (::db/data)
                         (::db/finished))
         out-text-actual (-> %
                             (:ret)
                             (:db)
                             (::db/exercise)
                             (::db/data)
                             (::db/text)
                             (::db/actual))]
     (if in-finished
       (= out-text-actual in-text-actual)
       true)))


(s/def ::backspace-removes-last-character
  #(let [ch (-> %
               (:args)
               (:event)
               (second))
        in-text-actual (-> %
                           (:args)
                           (:cofx)
                           (:db)
                           (::db/exercise)
                           (::db/data)
                           (::db/text)
                           (::db/actual))
        out-text-actual (-> %
                            (:ret)
                            (:db)
                            (::db/exercise)
                            (::db/data)
                            (::db/text)
                            (::db/actual))]
     (if (= \backspace ch)
       (= out-text-actual
          (if (empty? in-text-actual)
            []
            (pop in-text-actual)))
       true)))


(s/def ::does-not-exceed-expected-text-length
  #(let [ch (-> %
               (:args)
               (:event)
               (second))
        in-text-actual (-> %
                           (:args)
                           (:cofx)
                           (:db)
                           (::db/exercise)
                           (::db/data)
                           (::db/text)
                           (::db/actual))
         in-text-expected (-> %
                              (:args)
                              (:cofx)
                              (:db)
                              (::db/exercise)
                              (::db/data)
                              (::db/text)
                              (::db/expected))
        out-text-actual (-> %
                            (:ret)
                            (:db)
                            (::db/exercise)
                            (::db/data)
                            (::db/text)
                            (::db/actual))]
     (if (and (not= \backspace ch)
              (= (count in-text-actual)
                 (count in-text-expected)))
       (= out-text-actual in-text-actual)
       true)))


(s/def ::appends-new-character-after-last-character
  #(let [ch (-> %
               (:args)
               (:event)
               (second))
         in-text-actual (-> %
                            (:args)
                            (:cofx)
                            (:db)
                            (::db/exercise)
                            (::db/data)
                            (::db/text)
                            (::db/actual))
         in-text-expected (-> %
                              (:args)
                              (:cofx)
                              (:db)
                              (::db/exercise)
                              (::db/data)
                              (::db/text)
                              (::db/expected))
         in-finished (-> %
                         (:args)
                         (:cofx)
                         (:db)
                         (::db/exercise)
                         (::db/data)
                         (::db/finished))
        out-text-actual (-> %
                            (:ret)
                            (:db)
                            (::db/exercise)
                            (::db/data)
                            (::db/text)
                            (::db/actual))]
     (if (and (not in-finished)
              (not= \backspace ch)
              (< (count in-text-actual)
                 (count in-text-expected)))
       (= out-text-actual (conj in-text-actual ch))
       true)))


(s/def ::exercise-loading-fails-on-invalid-time
  #(let [in-db (-> %
                   :args
                   :cofx
                   :db)
         in-response-time (-> %
                              :args
                              :event
                              second
                              :data
                              :exercise
                              :time)
         in-response-text (-> %
                              :args
                              :event
                              second
                              :data
                              :exercise
                              :text)
         out-db (-> %
                    :ret
                    :db)
         out-dispatch (-> %
                          :ret
                          :db)]
     (if (and (not (s/valid? ::db/exercise-timer-initial
                             in-response-time))
              (s/valid? ::db/exercise-text
                        in-response-text))
       (and (= out-db
               in-db)
            (= (first out-dispatch)
               ::exercise-loading-failed)
            (str/includes? (second out-dispatch)
                           "invalid exercise time"))
       true)))


(s/def ::exercise-loading-fails-on-invalid-text
  #(let [in-db (-> %
                   :args
                   :cofx
                   :db)
         in-response-time (-> %
                              :args
                              :event
                              second
                              :data
                              :exercise
                              :time)
         in-response-text (-> %
                              :args
                              :event
                              second
                              :data
                              :exercise
                              :text)
         out-db (-> %
                    :ret
                    :db)
         out-dispatch (-> %
                          :ret
                          :db)]
     (if (and (not (s/valid? ::db/exercise-text
                             in-response-text))
              (s/valid? ::db/exercise-timer-initial
                        in-response-time))
       (s/and (= out-db
                 in-db)
              (= (first out-dispatch)
                 ::exercise-loading-failed)
              (str/includes? (second out-dispatch)
                             "invalid exercise-text"))
       true)))


(s/def ::exercise-state-loads
  #(let [in-time (-> %
                     :args
                     :event
                     second
                     :data
                     :exercise
                     :time)
         in-text (-> %
                     :args
                     :event
                     second
                     :data
                     :exercise
                     :text)
         out-time-current (-> %
                              :ret
                              :db
                              ::db/exercise
                              ::db/data
                              ::db/timer
                              ::db/current)
         out-time-initial (-> %
                              :ret
                              :db
                              ::db/exercise
                              ::db/data
                              ::db/timer
                              ::db/initial)
         out-text-expected (-> %
                               :ret
                               :db
                               ::db/exercise
                               ::db/data
                               ::db/text
                               ::db/expected)
         out-text-actual (-> %
                             :ret
                             :db
                             ::db/exercise
                             ::db/data
                             ::db/text
                             ::db/actual)]
     (if (and (s/valid? ::db/exercise-timer-initial
                        in-time)
              (s/valid? ::db/exercise-text
                        in-text))
       (s/and (= out-time-initial out-time-current in-time)
              (= out-text-expected in-text)
              (empty? out-text-actual))
       true)))


(s/def ::sends-get-exercise-request
  #(let [in-exercise-id (-> %
                          :args
                          :event
                          second)
         out-request (-> %
                         :ret
                         :http-xhrio)
         expected-request {:method :get
                           :uri config/api-base-url
                           :params {:query (str/format exercise-query-fmt
                                                       in-exercise-id)}
                           :timeout 5000
                           :on-success [::exercise-loading-succeed]
                           :on-failure [::exercise-loading-failed]}]
     (= (dissoc out-request :response-format)
        expected-request)))

(s/fdef
 character-typed
 :args (s/cat :cofx ::common-events/coeffects
              :event ::character-typed-event)
 :ret (s/and ::common-events/effects
             ::starts-exercise)
 :fn (s/and ::finishes-exercise-when-whole-text-matches
            ::ignores-typing-after-exercise-is-finished
            ::backspace-removes-last-character
            ::does-not-exceed-expected-text-length
            ::appends-new-character-after-last-character))
(defn character-typed [{:keys [db]}
                       [_ character]]
  (let [exercise-finished? (-> db
                               (::db/exercise)
                               (::db/data)
                               (::db/finished))
        text-expected (-> db
                          (::db/exercise)
                          (::db/data)
                          (::db/text)
                          (::db/expected))
        text-actual (-> db
                        (::db/exercise)
                        (::db/data)
                        (::db/text)
                        (::db/actual))
        next-text-actual (cond

                           (= \backspace character)
                           (if (empty? text-actual)
                             text-actual
                             (pop text-actual))

                           (= (count text-actual)
                              (count text-expected))
                           text-actual

                           :else
                           (conj text-actual character))]
    {:db (if exercise-finished?
           db
           (-> db
               (assoc-in [::db/exercise
                          ::db/data
                          ::db/started]
                         true)
               (assoc-in [::db/exercise
                          ::db/data
                          ::db/text
                          ::db/actual]
                         next-text-actual)
               (assoc-in [::db/exercise
                          ::db/data
                          ::db/finished]
                         (= next-text-actual text-expected))
          (assoc-in [::db/exercise
                     ::db/ui
                     ::db/summary-modal
                     ::common-db/visible]
                    (= next-text-actual text-expected))))}))

(rf/reg-event-fx
 ::character-typed
 character-typed)


(s/fdef
 timer-ticked
 :args (s/cat :cofx ::common-events/coeffects
              :event ::common-events/parameterless-event)
 :ret (s/and ::common-events/effects
             ::exercise-timer-does-not-go-below-zero)
 :fn (s/and ::exercise-timer-counts-down
            ::finishes-exercise-when-reaches-zero
            ::tick-ignored-if-exercise-not-started
            ::tick-ignored-if-exercise-finished))
(defn timer-ticked [{:keys [db]}
                     [_ _]]
  (let [exercise-started? (-> db
                             (::db/exercise)
                             (::db/data)
                             (::db/started))
        exercise-finished? (-> db
                             (::db/exercise)
                             (::db/data)
                             (::db/finished))
        timer (-> db
                  (::db/exercise)
                  (::db/data)
                  (::db/timer)
                  (::db/current))]
    {:db (if (or (not exercise-started?)
                 exercise-finished?)
           db
           (-> db
               (assoc-in [::db/exercise
                          ::db/data
                          ::db/timer
                          ::db/current]
                         (if (zero? timer)
                           0
                           (dec timer)))
               (assoc-in [::db/exercise
                          ::db/data
                          ::db/finished]
                         (zero? timer))
               (assoc-in [::db/exercise
                          ::db/ui
                          ::db/summary-modal
                          ::common-db/visible]
                         (zero? timer))))}))

(rf/reg-event-fx
 ::timer-ticked
 timer-ticked)


(s/fdef
 exercise-restarted
 :args (s/cat :cofx ::common-events/coeffects
              :event ::common-events/parameterless-event)
 :ret ::common-events/effects
 :fn ::exercise-state-resets)
(defn exercise-restarted [{:keys [db]}
                          [_ _]]
  {:db (-> db
           (assoc-in [::db/exercise
                      ::db/ui
                      ::db/summary-modal]
                     (-> db/default-db
                         (::db/exercise)
                         (::db/ui)
                         (::db/summary-modal)))
           (assoc-in [::db/exercise
                      ::db/data
                      ::db/started]
                     (-> db/default-db
                         (::db/exercise)
                         (::db/data)
                         (::db/started)))
           (assoc-in [::db/exercise
                      ::db/data
                      ::db/finished]
                     (-> db/default-db
                         (::db/exercise)
                         (::db/data)
                         (::db/finished)))
           (assoc-in [::db/exercise
                      ::db/data
                      ::db/text
                      ::db/actual]
                     (-> db/default-db
                         (::db/exercise)
                         (::db/data)
                         (::db/text)
                         (::db/actual)))
           (update-in [::db/exercise
                       ::db/data
                       ::db/timer]
                      #(assoc-in %
                                 [::db/current]
                                 (::db/initial %))))})

(rf/reg-event-fx
 ::exercise-restarted
 exercise-restarted)


(s/fdef
 exercise-loading-requested
 :args (s/cat :cofx ::common-events/coeffects
              :event ::exercise-loading-requested-event)
 :ret (s/and ::common-events/effects
             ::loader-shows-up)
 :fn ::sends-get-exercise-request)
(defn exercise-loading-requested [{:keys [db]}
                                  [_ exercise-id]]
  {:db (-> db
           (assoc-in [::db/exercise
                      ::db/ui
                      ::common-db/loader
                      ::common-db/visible]
                     true))
           :http-xhrio {:method :get
                        :uri config/api-base-url
                        :params {:query (str/format exercise-query-fmt
                                                    exercise-id)}
                        :timeout 5000
                        :response-format response-format
                        :on-success [::exercise-loading-succeed]
                        :on-failure [::exercise-loading-failed]}})


(rf/reg-event-fx
 ::exercise-loading-requested
 exercise-loading-requested)


(s/fdef
 exercise-loading-succeed
 :args (s/cat :cofx ::common-events/coeffects
              :event ::exercise-loading-succeed-event)
 :ret (s/and ::common-events/effects
             ::loader-hides-when-not-dispatching)
 :fn (s/and ::exercise-state-loads
            ::exercise-loading-fails-on-invalid-time
            ::exercise-loading-fails-on-invalid-text))
(defn exercise-loading-succeed [{:keys [db]}
                                [_ {{:keys [exercise]} :data}]]
  (let [exercise-time (:time exercise)
        exercise-text (-> exercise
                          :text
                          vec)]
    (cond (not (s/valid? ::db/exercise-timer-initial
                         exercise-time))
          (let [error-msg "invalid exercise time"
                error-details (s/explain-data ::db/exercise-timer-initial
                                              exercise-time)]
            {:db db
             :dispatch [::exercise-loading-failed
                        error-msg
                        error-details]})

          (not (s/valid? ::db/exercise-text
                         exercise-text))
          (let [error-msg "invalid exercise text"
                error-details (s/explain-data ::db/exercise-text
                                              exercise-text)]
            {:db db
             :dispatch [::exercise-loading-failed
                        error-msg
                        error-details]})

          :else
          {:db (-> db
                   (assoc-in [::db/exercise
                              ::db/ui
                              ::common-db/loader
                              ::common-db/visible]
                             false)
                   (assoc-in [::db/exercise
                              ::db/data]
                             (merge (-> db/default-db
                                        ::db/exercise
                                        ::db/data)
                                    {::db/timer {::db/current exercise-time
                                                 ::db/initial exercise-time}
                                     ::db/text {::db/actual []
                                                ::db/expected (-> exercise
                                                                  :text
                                                                  vec)}})))})))

(rf/reg-event-fx
 ::exercise-loading-succeed
 exercise-loading-succeed)


(s/fdef
 exercise-loading-failed
 :args (s/cat :cofx ::common-events/coeffects
              :event ::common-events/failure-event)
 :ret (s/and ::common-events/effects
             ::loader-hides))
(defn exercise-loading-failed [{:keys [db]}
                               [_ [message details]]]
  {:db (-> db
           (assoc-in [::db/exercise
                      ::db/ui
                      ::common-db/loader
                      ::common-db/visible]
                     false))})

(rf/reg-event-fx
 ::exercise-loading-failed
 exercise-loading-failed)
