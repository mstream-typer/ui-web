(ns typer-ui-web.home.events
  (:require [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
            [ajax.core :as ajax]
            [cemerick.url :as url]
            [cuerdas.core :as str]
            [day8.re-frame.http-fx]
            [typer-ui-web.config :as config]
            [typer-ui-web.home.db :as db]
            [typer-ui-web.common.db :as common-db]
            [typer-ui-web.common.core :refer [evt> <sub]]
            [typer-ui-web.common.events :as common-events]))


(def courses-query-fmt
  "{courses{id name description}}")


(s/def ::id
  (s/and string? (comp pos? count)))


(s/def ::name
  string?)


(s/def ::description
  string?)


(s/def ::course
  (s/keys :req-un [::id ::name ::description]))


(s/def ::courses
  (s/coll-of ::course :kind vector?))

(s/def ::data
  (s/keys :req-un [::courses]))


(s/def ::courses-response
  (s/keys :req-un [::data]))


(s/def ::courses-loading-requested-event
  (s/tuple keyword?))


(s/def ::courses-loading-succeed-event
  (s/tuple keyword?
           ::courses-response))


(s/def ::loader-shows-up
  (comp ::common-db/visible
        ::common-db/loader
        ::db/ui
        ::db/home
        :db))


(s/def ::loader-hides
  (comp not
        ::common-db/visible
        ::common-db/loader
        ::db/ui
        ::db/home
        :db))


(s/def ::courses-state-loads
  #(let [in-courses (-> %
                     :args
                     :event
                     second
                     :data
                     :courses)
         out-courses (-> %
                         :ret
                         :db
                         ::db/home
                         ::db/data
                         ::db/courses)
         expected-courses (map (fn [exercise]
                                 {::common-db/id (:id exercise)
                                  ::db/name (:name exercise)
                                  ::db/description (:description exercise)})
                               in-courses)]
     (= out-courses
        expected-courses)))


(s/def ::sends-get-courses-request
  #(let [out-request (-> %
                         :ret
                         :http-xhrio)
         expected-request {:method :get
                           :uri config/api-base-url
                           :params {:query courses-query-fmt}
                           :timeout 5000
                           :on-success [::courses-loading-succeed]
                           :on-failure [::courses-loading-failed]}]
     (= (dissoc out-request :response-format)
        expected-request)))


(s/fdef
 courses-loading-requested
 :args (s/cat :cofx ::common-events/coeffects
              :event ::courses-loading-requested-event)
 :ret (s/and ::common-events/effects
             ::loader-shows-up)
 :fn ::sends-get-courses-request)
(defn courses-loading-requested [{:keys [db]}
                                 _]
  {:db (-> db
           (assoc-in [::db/home
                      ::db/ui
                      ::common-db/loader
                      ::common-db/visible]
                     true))
   :http-xhrio {:method :get
                :uri config/api-base-url
                :params {:query courses-query-fmt}
                :timeout 5000
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [::courses-loading-succeed]
                :on-failure [::courses-loading-failed]}})

(rf/reg-event-fx
 ::courses-loading-requested
 courses-loading-requested)


(s/fdef
 courses-loading-succeed
 :args (s/cat :cofx ::common-events/coeffects
              :event ::courses-loading-succeed-event)
 :ret (s/and ::common-events/effects
             ::loader-hides)
 :fn ::courses-state-loads)
(defn courses-loading-succeed [{:keys [db]}
                              [_ {{:keys [courses]} :data}]]
  {:db (-> db
           (assoc-in [::db/home
                      ::db/ui
                      ::common-db/loader
                      ::common-db/visible]
                     false)
           (assoc-in [::db/home
                      ::db/data
                      ::db/courses]
                     (mapv #(hash-map ::common-db/id (:id %)
                                      ::db/name (:name %)
                                      ::db/description (:description %))
                           courses)))})


(rf/reg-event-fx
 ::courses-loading-succeed
 courses-loading-succeed)


(s/fdef
 courses-loading-failed
 :args (s/cat :cofx ::common-events/coeffects
              :event ::common-events/failure-event)
 :ret (s/and ::common-events/effects
             ::loader-hides))
(defn courses-loading-failed [{:keys [db]}
                              [_ error]]
  {:db (-> db
           (assoc-in [::db/home
                      ::db/ui
                      ::common-db/loader
                      ::common-db/visible]
                     false))})

(rf/reg-event-fx
 ::courses-loading-failed
 courses-loading-failed)
