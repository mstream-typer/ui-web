(ns typer-ui-web.events
  (:require [re-frame.core :as rf]
            [typer-ui-web.common :refer [evt> <sub]]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [typer-ui-web.db :as db]
            [typer-ui-web.exercise.db :as exercise-db]))


(s/def ::text
  ::exercise-db/exercise-text)


(s/def ::time
  ::exercise-db/exercise-timer-initial)


(s/def ::exercise
  (s/keys :req [::text ::time]))
  

(s/def ::parameterless-event
  (s/tuple keyword?))


(s/def ::input-change-event
  (s/tuple keyword? string?))


(s/def ::failure-event
  (s/tuple keyword? string?))


(s/def ::navigated-to-exercise-success-event
  (s/tuple keyword? ::exercise))


(s/def ::on-success (s/tuple keyword?))


(s/def ::on-failure (s/tuple keyword?)) 


(s/def ::load-exercise
  (s/keys ::on-success ::on-failure))


(s/fdef
 db-initialized
 :args (s/cat :db ::db/db 
              :event ::parameterless-event)
 :ret (s/and ::db/db
             (partial = db/default-db)))
(defn db-initialized [_ _]
  db/default-db)

(rf/reg-event-db
 ::db-initialized
 db-initialized)


(s/fdef 
 login-menu-button-pressed
 :args (s/cat :db ::db/db  
              :event ::parameterless-event)
 :ret ::db/db
 :fn #(let [out-login-menu-visible (-> % :ret
                                       ::db/ui
                                       ::db/login-menu
                                       ::db/visible)]
        out-login-menu-visible))
(defn login-menu-button-pressed [db [_ character]] 
  (-> db
      (assoc-in [::db/ui ::db/login-menu ::db/visible] true)))

(rf/reg-event-db
 ::login-menu-button-pressed
 login-menu-button-pressed)


(s/fdef 
 cancel-login-menu-button-pressed
 :args (s/cat :db ::db/db  
              :event ::parameterless-event)
 :ret ::db/db
 :fn #(let [out-login-menu-visible (-> % :ret
                                       ::db/ui
                                       ::db/login-menu
                                       ::db/visible)]
        (not out-login-menu-visible)))
(defn cancel-login-menu-button-pressed [db [_ character]] 
  (-> db
      (assoc-in [::db/ui ::db/login-menu ::db/visible] false)))

(rf/reg-event-db
 ::cancel-login-menu-button-pressed
 cancel-login-menu-button-pressed)


(s/fdef 
 login-menu-username-changed
 :args (s/cat :db ::db/db  
              :event ::input-change-event)
 :ret ::db/db
 :fn #(let [in-username (-> %
                            :args
                            :event
                            second)
            out-login-menu-username (-> %
                                        :ret
                                        ::db/ui
                                        ::db/login-menu
                                        ::db/username)]
        (= out-login-menu-username in-username)))
(defn login-menu-username-changed [db [_ username]]
  (-> db
      (assoc-in [::db/ui ::db/login-menu ::db/username] username)))

(rf/reg-event-db
 ::login-menu-username-changed
 login-menu-username-changed)


(s/fdef 
 login-menu-password-changed
 :args (s/cat :db ::db/db  
              :event ::input-change-event)
 :ret ::db/db
 :fn #(let [in-password (-> %
                            :args
                            :event
                            second)
            out-login-menu-password (-> %
                                        :ret
                                        ::db/ui
                                        ::db/login-menu
                                        ::db/password)]
        (= out-login-menu-password in-password)))
(defn login-menu-password-changed [db [_ password]]
  (-> db
      (assoc-in [::db/ui ::db/login-menu ::db/password] password)))

(rf/reg-event-db
 ::login-menu-password-changed
 login-menu-password-changed)


(s/fdef 
 navigated-to-home
 :args (s/cat :db ::db/db  
              :event ::parameterless-event)
 :ret ::db/db
 :fn #(let [out-view (-> %
                         :ret
                         ::db/ui
                         ::db/view)]
        (= out-view ::db/home)))
(defn navigated-to-home [db _]
  (-> db
      (assoc-in [::db/ui
                 ::db/view]
                ::db/home)))

(rf/reg-event-db
 ::navigated-to-home
 navigated-to-home)


(s/def ::view-changes-to-exercise 
  #(= ::db/exercise
      (-> %
          (:ret)
          (::db/ui)
          (::db/view))))


(s/def ::exercise-state-resets 
  #(= (::exercise-db/exercise exercise-db/default-db)
      (-> %
          (:ret)
          (::exercise-db/exercise))))


(s/def ::exercise-state-loads
  #(let [in-loaded-exercise (-> %
                             (:args)
                             (:event)
                             (second))
         out-exercise-data (-> %
                               (:ret)
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
       (:ret)
       (:db)
       (::db/ui)
       (::db/loader)
       (::db/visible)))


(s/def ::loader-hides
  #(-> %
       (:ret)
       (:db)
       (::db/ui)
       (::db/loader)
       (::db/visible)
       (not)))


(s/fdef 
 navigated-to-exercise
 :args (s/cat :db ::db/db  
              :event ::parameterless-event)
 :ret (s/keys ::db/db ::load-exercise)
 :fn ::loader-shows-up)
(defn navigated-to-exercise [{:keys [db]} [_ exercise-id]]
  {:db (assoc-in db
                 [::db/ui
                  ::db/loader
                  ::db/visible]
                 true)
   ::load-exercise {:exercise-id exercise-id 
                    :on-success [::navigated-to-exercise-success]
                    :on-failure [::navigated-to-exercise-failure]}})

(rf/reg-event-fx
 ::navigated-to-exercise
 navigated-to-exercise)


(s/fdef 
 navigated-to-exercise-success
 :args (s/cat :db ::db/db  
              :event ::navigated-to-exercise-success-event)
 :ret ::db/db
 :fn (s/and ::view-changes-to-exercise
            ::exercise-state-loads
            ::loader-hides))
(defn navigated-to-exercise-success [db [_ exercise]]
  (-> db
      (assoc-in [::db/ui
                 ::db/loader
                 ::db/visible]
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
                                       (::time exercise)))))))

(rf/reg-event-db
 ::navigated-to-exercise-success
 navigated-to-exercise-success)


(s/fdef 
 navigated-to-exercise-failure
 :args (s/cat :db ::db/db  
              :event ::failure-event)
 :ret ::db/db
 :fn ::loader-hides)
(defn navigated-to-exercise-failure [db [_ error]]
  (println error)
  (-> db
      (assoc-in [::db/ui
                 ::db/loader
                 ::db/visible]
                false)))

(rf/reg-event-db
 ::navigated-to-exercise-failure
 navigated-to-exercise-failure)


(defn load-exercise [{:keys [exercise-id on-success on-failure]}]
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
                                (gen/vector char-gen text-len)
                                100)]
    (js/setTimeout (fn []
                     (try (let [text (gen/generate text-gen)]
                            (evt> (conj on-success 
                                        {::text text
                                         ::time (* 2 text-len)})))
                          (catch js/Object ex
                            (evt> (conj on-failure ex)))))
                   500)))
 
(rf/reg-fx
 ::load-exercise
 load-exercise)


