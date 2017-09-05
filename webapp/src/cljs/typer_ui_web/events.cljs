(ns typer-ui-web.events
  (:require [re-frame.core :as rf]
            [typer-ui-web.common.core :refer [evt> <sub]]
            [typer-ui-web.common.events :as common-events]
            [typer-ui-web.course.events :as course-events]
            [typer-ui-web.exercise.events :as exercise-events]
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


(s/def ::db-initialized-with-default-values
  (partial = {:db db/default-db}))


(s/def ::dispatches-course-loading-request
  #(let [in-course-id (-> %
                          :args
                          :event
                          second)
         out-dispatched-event (-> %
                                  :ret
                                  :dispatch)]
     (= out-dispatched-event
        [::course-events/course-loading-requested in-course-id])))


(s/def ::dispatches-exercise-loading-request
  #(let [in-exercise-id (-> %
                          :args
                          :event
                          second)
         out-dispatched-event (-> %
                                  :ret
                                  :dispatch)]
     (= out-dispatched-event
        [::exercise-events/exercise-loading-requested in-exercise-id])))


(s/fdef
 db-initialized
 :args (s/cat :cofx ::common-events/coeffects
              :event ::common-events/parameterless-event)
 :ret (s/and ::common-events/effects
             ::db-initialized-with-default-values))
(defn db-initialized [_ _]
  {:db db/default-db})

(rf/reg-event-fx
 ::db-initialized
 db-initialized)



(s/fdef 
 navigated-to-home
 :args (s/cat :cofx ::common-events/coeffects
              :event ::common-events/parameterless-event)
 :ret ::common-events/effects
 :fn #(let [out-view (-> %
                         :ret
                         :db
                         ::db/ui
                         ::db/view)]
        (= out-view ::db/home)))
(defn navigated-to-home [{:keys [db]}
                         _]
  {:db (-> db
           (assoc-in [::db/ui
                      ::db/view]
                     ::db/home))})

(rf/reg-event-fx
 ::navigated-to-home
 navigated-to-home)


(s/fdef 
 navigated-to-course
 :args (s/cat :cofx ::common-events/coeffects
              :event ::navigated-to-course-event)
 :ret (s/and ::common-events/effects
             ::view-changes-to-course)
 :fn ::dispatches-course-loading-request)
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
 navigated-to-exercise
 :args (s/cat :cofx ::common-events/coeffects
              :event ::navigate-to-exercise-requested-event)
 :ret (s/and ::common-events/effects
             ::view-changes-to-exercise)
 :fn ::dispatches-exercise-loading-request)
(defn navigated-to-exercise [{:keys [db]}
                             [_ exercise-id]]
  {:db (-> db
           (assoc-in [::db/ui
                      ::db/view]
                     ::db/exercise))
   :dispatch [::exercise-events/exercise-loading-requested exercise-id]})

(rf/reg-event-fx
 ::navigated-to-exercise
 navigated-to-exercise)
