(ns typer-ui-web.events
  (:require [re-frame.core :as rf]
            [typer-ui-web.common.core :refer [evt> <sub]]
            [typer-ui-web.common.events :as common-events]
            [typer-ui-web.course.events :as course-events]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [typer-ui-web.db :as db]
            [typer-ui-web.common.db :as common-db]
            [typer-ui-web.exercise.db :as exercise-db]))


(s/def ::text
  ::exercise-db/exercise-text)


(s/def ::time
  ::exercise-db/exercise-timer-initial)


(s/def ::exercise
  (s/keys :req [::text
                ::time])) 


(s/def ::navigated-to-course-event
  (s/tuple keyword?
           string?))


(s/def ::navigate-to-exercise-requested-event
  (s/tuple keyword?
           string?))


(s/def ::navigate-to-exercise-succeed-event
  (s/tuple keyword?
           ::exercise))


(s/def ::exercise-id 
  string?)


(s/def ::on-success
  (s/tuple keyword?))


(s/def ::on-failure
  (s/tuple keyword?)) 


(s/def ::load-exercise
  (s/keys :req [::exercise-id
                ::on-success
                ::on-failure]))


(s/def ::view-changes-to-course
  #(= ::db/course
      (-> %
          (:db)
          (::db/ui)
          (::db/view))))


(s/def ::view-changes-to-exercise 
  #(= ::db/exercise
      (-> %
          (:db)
          (::db/ui)
          (::db/view))))


(s/def ::exercise-state-resets 
  #(= (::exercise-db/exercise exercise-db/default-db)
      (-> %
          (:db)
          (::exercise-db/exercise))))


(s/def ::exercise-state-loads
  #(let [in-loaded-exercise (-> %
                             (:args)
                             (:event)
                             (second))
         out-exercise-data (-> %
                               (:ret)
                               (:db)
                               (::exercise-db/exercise)
                               (::exercise-db/data))
         default-exercise-data (-> exercise-db/default-db
                                   (::exercise-db/exercise)
                                   (::exercise-db/data))]
     (= out-exercise-data
        (-> default-exercise-data
            (assoc-in [::exercise-db/text
                       ::exercise-db/expected]
                      (::text in-loaded-exercise))
            (assoc-in [::exercise-db/timer
                       ::exercise-db/initial]
                      (::time in-loaded-exercise))
            (assoc-in [::exercise-db/timer
                       ::exercise-db/current]
                      (::time in-loaded-exercise))))))


(s/def ::loader-shows-up
  #(-> %
       (:db)
       (::db/ui)
       (::common-db/loader)
       (::common-db/visible)))


(s/def ::loader-hides
  #(-> %
       (:db)
       (::db/ui)
       (::common-db/loader)
       (::common-db/visible)
       (not)))


(s/fdef
 db-initialized
 :args (s/cat :cofx ::common-events/coeffects
              :event ::common-events/parameterless-event)
 :ret (s/and ::common-events/effects
             (partial = {:db db/default-db})))
(defn db-initialized [_ _]
  {:db db/default-db})

(rf/reg-event-fx
 ::db-initialized
 db-initialized)


(s/fdef 
 navigated-to-course
 :args (s/cat :cofx ::common-events/coeffects
              :event ::navigated-to-course-event)
 :ret (s/and ::common-events/effects
             ::view-changes-to-course))
(defn navigated-to-course [{:keys [db]}
                           [_ course-id]]
  {:db (-> db
           (assoc-in [::db/ui
                      ::db/view]
                     ::db/course))
   :dispatch [::course-events/course-loading-requested course-id]})


(rf/reg-event-fx
 ::navigated-to-course
 navigated-to-course)


(s/fdef 
 navigate-to-home-requested
 :args (s/cat :cofx ::common-events/coeffects
              :event ::common-events/parameterless-event)
 :ret ::common-events/effects
 :fn #(let [out-view (-> %
                         :ret
                         :db
                         ::db/ui
                         ::db/view)]
        (= out-view ::db/home)))
(defn navigate-to-home-requested [{:keys [db]}
                                  _]
  {:db (-> db
           (assoc-in [::db/ui
                      ::db/view]
                     ::db/home))})

(rf/reg-event-fx
 ::navigate-to-home-requested
 navigate-to-home-requested)


(s/fdef 
 navigate-to-exercise-requested
 :args (s/cat :cofx ::common-events/coeffects
              :event ::navigate-to-exercise-requested-event)
 :ret (s/and ::common-events/effects
             ::loader-shows-up
             (s/keys :req [::load-exercise])))
(defn navigate-to-exercise-requested [{:keys [db]}
                                      [_ exercise-id]]
  {:db (-> db
           (assoc-in [::db/ui
                      ::common-db/loader
                      ::common-db/visible]
                     true))
   ::load-exercise {::exercise-id exercise-id 
                    ::on-success [::navigate-to-exercise-succee]
                    ::on-failure [::navigate-to-exercise-failed]}})

(rf/reg-event-fx
 ::navigate-to-exercise-requested
 navigate-to-exercise-requested)


(s/fdef 
 navigate-to-exercise-succeed
 :args (s/cat :cofx ::common-events/coeffects
              :event ::navigate-to-exercise-succeed-event)
 :ret (s/and ::common-events/effects
             ::loader-hides
             ::view-changes-to-exercise)
 :fn ::exercise-state-loads)
(defn navigate-to-exercise-succeed [{:keys [db]}
                                    [_ exercise]]
  {:db (-> db
           (assoc-in [::db/ui
                      ::common-db/loader
                      ::common-db/visible]
                     false)
           (assoc-in [::db/ui
                      ::db/view]
                     ::db/exercise)
           (merge (update-in exercise-db/default-db
                             [::exercise-db/exercise
                              ::exercise-db/data]
                             #(-> %
                                  (assoc-in [::exercise-db/text
                                             ::exercise-db/expected]
                                            (::text exercise))
                                  (assoc-in [::exercise-db/timer
                                             ::exercise-db/initial]
                                            (::time exercise))
                                  (assoc-in [::exercise-db/timer
                                             ::exercise-db/current]
                                            (::time exercise))))))})

(rf/reg-event-fx
 ::navigate-to-exercise-succeed
 navigate-to-exercise-succeed)


(s/fdef 
 navigate-to-exercise-failed
 :args (s/cat :cofx ::common-events/coeffects
              :event ::common-events/failure-event)
 :ret (s/and ::common-events/effects
             ::loader-hides))
(defn navigate-to-exercise-failed [{:keys [db]}
                                   [_ error]]
  {:db (-> db
           (assoc-in [::db/ui
                      ::common-db/loader
                      ::common-db/visible]
                     false))})

(rf/reg-event-fx
 ::navigate-to-exercise-failed
 navigate-to-exercise-failed)


(defn load-exercise [{:keys [::exercise-id
                             ::on-success
                             ::on-failure]}]
  (let [text-len 50
        exercise-chars {"1" #{\f \j}
                        "2" #{\d \k}
                        "3" #{\s \l}
                        "4" #{\a \;}
                        "5" #{\e \i}}
        charset (get exercise-chars
                     exercise-id
                     exercise-db/characters)
        char-gen (gen/frequency [[1 (gen/return \newline)]
                                 [5 (gen/return \space)]
                                 [25 (gen/elements charset)]])
        text-gen (gen/such-that (partial s/valid?
                                         ::exercise-db/exercise-text)
                                (gen/vector char-gen
                                            text-len)
                                100)]
    (js/setTimeout #(try (let [text (gen/generate text-gen)]
                           (evt> (conj on-success 
                                       {::text text
                                        ::time (* 2 text-len)})))
                         (catch js/Object ex
                           (evt> (conj on-failure
                                       ex))))
                   500)))
 
(rf/reg-fx
 ::load-exercise
 load-exercise)
