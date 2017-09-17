(ns typer-ui-web.course.events
  (:require [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
            [ajax.core :as ajax]
            [cemerick.url :as url]
            [cuerdas.core :as str]
            [day8.re-frame.http-fx]
            [typer-ui-web.config :as config]
            [typer-ui-web.course.db :as db]
            [typer-ui-web.common.db :as common-db]
            [typer-ui-web.common.core :refer [evt> <sub]]
            [typer-ui-web.common.events :as common-events]))


(def course-query-fmt
  "{course(id:%s){name exercises{id name description}}}")


(s/def ::id
  (s/and string? (comp pos? count)))


(s/def ::name
  string?)


(s/def ::description
  string?)


(s/def ::exercise
  (s/keys :req-un [::id ::name ::description]))


(s/def ::exercises
  (s/coll-of ::exercise :kind vector?))


(s/def ::course
  (s/keys :req-un [::name ::exercises]))


(s/def ::data
  (s/keys :req-un [::course]))


(s/def ::course-response
  (s/keys :req-un [::data]))


(s/def ::course-loading-requested-event
  (s/tuple keyword?
           string?))


(s/def ::course-loading-succeed-event
  (s/tuple keyword?
           ::course-response))


(s/def ::loader-shows-up
  (comp ::common-db/visible
        ::common-db/loader
        ::db/ui
        ::db/course
        :db))


(s/def ::loader-hides
  (comp not
        ::common-db/visible
        ::common-db/loader
        ::db/ui
        ::db/course
        :db))


(s/def ::course-state-loads
  #(let [in-name (-> %
                     :args
                     :event
                     second
                     :data
                     :course
                     :name)
         in-exercises (-> %
                          :args
                          :event
                          second
                          :data
                          :course
                          :exercises)
         out-name (-> %
                      :ret
                      :db
                      ::db/course
                      ::db/data
                      ::db/name)
         out-exercises (-> %
                           :ret
                           :db
                           ::db/course
                           ::db/data
                           ::db/exercises)
         expected-exercises (map (fn [exercise]
                                   {::db/id (:id exercise)
                                    ::db/name (:name exercise)
                                    ::db/description (:description exercise)})
                                 in-exercises)]
     (s/and (= out-name
               in-name)
            (= out-exercises
               expected-exercises))))


(s/def ::sends-get-course-request
  #(let [in-course-id (-> %
                          :args
                          :event
                          second)
         out-request (-> %
                         :ret
                         :http-xhrio)
         expected-request {:method :get
                           :uri config/api-base-url
                           :params {:query (str/format course-query-fmt
                                                       in-course-id)}
                           :timeout 5000
                           :on-success [::course-loading-succeed]
                           :on-failure [::course-loading-failed]}]
     (= (dissoc out-request :response-format)
        expected-request)))


(s/fdef
 course-loading-requested
 :args (s/cat :cofx ::common-events/coeffects
              :event ::course-loading-requested-event)
 :ret (s/and ::common-events/effects
             ::loader-shows-up)
 :fn ::sends-get-course-request)
(defn course-loading-requested [{:keys [db]}
                                [_ course-id]]
  {:db (-> db
           (assoc-in [::db/course
                      ::db/ui
                      ::common-db/loader
                      ::common-db/visible]
                     true))
   :http-xhrio {:method :get
                :uri config/api-base-url
                :params {:query (str/format course-query-fmt
                                            course-id)}
                :timeout 5000
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [::course-loading-succeed]
                :on-failure [::course-loading-failed]}})

(rf/reg-event-fx
 ::course-loading-requested
 course-loading-requested)


(s/fdef
 course-loading-succeed
 :args (s/cat :cofx ::common-events/coeffects
              :event ::course-loading-succeed-event)
 :ret (s/and ::common-events/effects
             ::loader-hides)
 :fn ::course-state-loads)
(defn course-loading-succeed [{:keys [db]}
                              [_ {{:keys [course]} :data}]]
  {:db (-> db
           (assoc-in [::db/course
                      ::db/ui
                      ::common-db/loader
                      ::common-db/visible]
                     false)
           (assoc-in [::db/course
                      ::db/data
                      ::db/name]
                     (:name course))
           (assoc-in [::db/course
                      ::db/data
                      ::db/exercises]
                     (mapv #(hash-map ::common-db/id (:id %)
                                      ::db/name (:name %)
                                      ::db/description (:description %))
                           (:exercises course))))})


(rf/reg-event-fx
 ::course-loading-succeed
 course-loading-succeed)


(s/fdef
 course-loading-failed
 :args (s/cat :cofx ::common-events/coeffects
              :event ::common-events/failure-event)
 :ret (s/and ::common-events/effects
             ::loader-hides))
(defn course-loading-failed [{:keys [db]}
                             [_ error]]
  {:db (-> db
           (assoc-in [::db/course
                      ::db/ui
                      ::common-db/loader
                      ::common-db/visible]
                     false))})

(rf/reg-event-fx
 ::course-loading-failed
 course-loading-failed)
