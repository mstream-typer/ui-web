(ns typer-ui-web.events
  (:require [re-frame.core :as rf]
            [typer-ui-web.common :refer [evt> <sub]]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [typer-ui-web.db :as db]
            [typer-ui-web.exercise.db :as exercise-db]))


(s/def ::credentials
  (s/keys :req [::db/username
                ::db/password]))


(s/def ::event-handler-db
  (s/keys :req-un [::db/db]))


(s/def ::coeffects
  ::event-handler-db)


(s/def ::effects
  ::event-handler-db)


(s/def ::text
  ::exercise-db/exercise-text)


(s/def ::time
  ::exercise-db/exercise-timer-initial)
(s/def ::exercise
  (s/keys :req [::text
                ::time])) 
  

(s/def ::parameterless-event
  (s/tuple keyword?))


(s/def ::user-signed-in-event
  (s/tuple keyword?
           ::credentials))


(s/def ::user-signed-in-success-event
  (s/tuple keyword?
           ::credentials))


(s/def ::input-change-event
  (s/tuple keyword?
           string?))


(s/def ::failure-event
  (s/tuple keyword?
           string?))


(s/def ::navigated-to-exercise-event
  (s/tuple keyword?
           string?))


(s/def ::navigated-to-exercise-success-event
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


(s/def ::sign-in
  (s/keys :req [::credentials
                ::on-success
                ::on-failure]))


(s/def ::login-menu-visible
  (comp ::db/visible
        ::db/login-menu
        ::db/ui
        :db))


(s/def ::login-menu-not-visible
  (comp not
        ::db/visible
        ::db/login-menu
        ::db/ui
        :db))


(s/def ::login-menu-loader-visible
  (comp ::db/visible
        ::db/loader
        ::db/login-menu
        ::db/ui
        :db))


(s/def ::login-menu-loader-not-visible
  (comp not
        ::db/visible
        ::db/loader
        ::db/login-menu
        ::db/ui
        :db))


(s/def ::user-dropdown-switched
  #(let [in-dropdown-visible (-> %
                                 (:args)
                                 (:cofx)
                                 (:db)
                                 (::db/ui)
                                 (::db/main-menu)
                                 (::db/user-dropdown)
                                 (::db/visible))
         out-dropdown-visible (-> %
                                  (:ret)
                                  (:db)
                                  (::db/ui)
                                  (::db/main-menu)
                                  (::db/user-dropdown)
                                  (::db/visible))]
     (not= out-dropdown-visible
           in-dropdown-visible)))










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


(s/def ::login-menu-inputs-clear
  #(let [login-menu (-> %
                        (:db)
                        (::db/ui)
                        (::db/login-menu))]
     (and (empty? (::db/username login-menu))
          (empty? (::db/password login-menu)))))


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
       (::db/loader)
       (::db/visible)))


(s/def ::loader-hides
  #(-> %
       (:db)
       (::db/ui)
       (::db/loader)
       (::db/visible)
       (not)))


(s/def ::sets-credentials-from-event
  #(let [event-creds (-> %
                         (:args)
                         (:event)
                         (second))
         user-creds (-> %
                        (:ret)
                        (:db)
                        (::db/user))]
     (= user-creds
        event-creds)))


(s/def ::resets-credentials
  #(let [default-creds (::db/user db/default-db)]
     (= default-creds
        (-> %
            (:db)
            (::db/user)))))


(s/def ::causes-sign-in-effect
  #(let [event-creds (-> %
                         (:args)
                         (:event)
                         (second))
         sign-in-effect-creds (-> %
                            (:ret)
                            (::sign-in)
                            (::credentials))]
     (= event-creds
        sign-in-effect-creds)))


(s/fdef
 db-initialized
 :args (s/cat :cofx ::coeffects
              :event ::parameterless-event)
 :ret (s/and ::effects
             (partial = {:db db/default-db})))
(defn db-initialized [_ _]
  {:db db/default-db})

(rf/reg-event-fx
 ::db-initialized
 db-initialized)


(s/fdef 
 user-dropdown-switched
 :args (s/cat :cofx ::coeffects
              :event ::parameterless-event)
 :ret ::effects
 :fn ::user-dropdown-switched)
(defn user-dropdown-switched [{:keys [db]}
                              _] 
  {:db (-> db
           (update-in [::db/ui
                       ::db/main-menu
                       ::db/user-dropdown
                       ::db/visible]
                     not))})

(rf/reg-event-fx
 ::user-dropdown-switched
 user-dropdown-switched)


(s/fdef 
 login-menu-button-pressed
 :args (s/cat :cofx ::coeffects
              :event ::parameterless-event)
 :ret (s/and ::effects
             ::login-menu-visible))
(defn login-menu-button-pressed [{:keys [db]}
                                 [_ character]] 
  {:db (-> db
           (assoc-in [::db/ui
                      ::db/login-menu
                      ::db/visible]
                     true))})

(rf/reg-event-fx
 ::login-menu-button-pressed
 login-menu-button-pressed)


(s/fdef 
 cancel-login-menu-button-pressed
 :args (s/cat :cofx ::coeffects
              :event ::parameterless-event)
 :ret (s/and ::effects
             ::login-menu-not-visible))
(defn cancel-login-menu-button-pressed [{:keys [db]}
                                        _] 
  {:db (-> db
           (assoc-in [::db/ui
                      ::db/login-menu
                      ::db/visible]
                     false))})

(rf/reg-event-fx
 ::cancel-login-menu-button-pressed
 cancel-login-menu-button-pressed)


(s/fdef 
 user-signed-in
 :args (s/cat :cofx ::coeffects  
              :event ::user-signed-in-event)
 :ret (s/and ::coeffects
             ::login-menu-visible
             ::login-menu-loader-visible
             (s/keys :req [::sign-in]))
 :fn ::causes-sign-in-effect)
(defn user-signed-in [{:keys [db]}
                      [_ creds]]
    {:db (-> db
             (assoc-in [::db/ui
                        ::db/login-menu
                        ::db/visible]
                       true)
             (assoc-in [::db/ui
                        ::db/login-menu
                        ::db/loader
                        ::db/visible]
                       true))
     ::sign-in {::credentials creds
                ::on-success [::user-signed-in-success]
                ::on-failure [::user-signed-in-failure]}})

(rf/reg-event-fx
 ::user-signed-in
 user-signed-in)


(s/fdef 
 user-signed-in-success
 :args (s/cat :cofx ::coeffects
              :event ::user-signed-in-success-event)
 :ret (s/and ::effects
             ::login-menu-not-visible
             ::login-menu-loader-not-visible)
 :fn ::sets-credentials-from-event)
(defn user-signed-in-success [{:keys [db]}
                              [_ credentials]]
  {:db (-> db 
           (assoc-in [::db/ui
                      ::db/login-menu
                      ::db/visible]
                     false)
           (assoc-in [::db/ui
                      ::db/login-menu
                      ::db/loader
                      ::db/visible]
                     false)
           (assoc-in [::db/user]
                     credentials))})

(rf/reg-event-fx
 ::user-signed-in-success
 user-signed-in-success)


(s/fdef 
 user-signed-in-failure
 :args (s/cat :cofx ::coeffects
              :event ::parameterless-event)
 :ret (s/and ::effects
             ::login-menu-not-visible
             ::login-menu-loader-not-visible))
(defn user-signed-in-failure [{:keys [db]}
                              _] 
  {:db (-> db
           (assoc-in [::db/ui
                      ::db/login-menu
                      ::db/visible]
                     false)
           (assoc-in [::db/ui
                      ::db/login-menu
                      ::db/loader
                      ::db/visible]
                     false))})

(rf/reg-event-fx
 ::user-signed-in-failure
 user-signed-in-success)


(s/fdef 
 user-signed-out
 :args (s/cat :cofx ::coeffects  
              :event ::parameterless-event)
 :ret (s/and ::coeffects
             ::resets-credentials))
(defn user-signed-out [{:keys [db]}
                      _]
    {:db (-> db
             (assoc-in [::db/user]
                       (::db/user db/default-db)))})

(rf/reg-event-fx
 ::user-signed-out
 user-signed-out)


(s/fdef 
 login-menu-username-changed
 :args (s/cat :cofx ::coeffects
              :event ::input-change-event)
 :ret ::effects
 :fn #(let [in-username (-> %
                            :args
                            :event
                            second)
            out-login-menu-username (-> %
                                        :ret
                                        :db
                                        ::db/ui
                                        ::db/login-menu
                                        ::db/username)]
        (= out-login-menu-username in-username)))
(defn login-menu-username-changed [{:keys [db]}
                                   [_ username]]
  {:db (-> db
           (assoc-in [::db/ui
                      ::db/login-menu
                      ::db/username] username))})

(rf/reg-event-fx
 ::login-menu-username-changed
 login-menu-username-changed)


(s/fdef 
 login-menu-password-changed
 :args (s/cat :cofx ::coeffects
              :event ::input-change-event)
 :ret ::effects
 :fn #(let [in-password (-> %
                            :args
                            :event
                            second)
            out-login-menu-password (-> %
                                        :ret
                                        :db
                                        ::db/ui
                                        ::db/login-menu
                                        ::db/password)]
        (= out-login-menu-password
           in-password)))
(defn login-menu-password-changed [{:keys [db]}
                                   [_ password]]
  {:db (-> db
           (assoc-in [::db/ui
                      ::db/login-menu
                      ::db/password]
                     password))})

(rf/reg-event-fx
 ::login-menu-password-changed
 login-menu-password-changed)


(s/fdef 
 navigated-to-home
 :args (s/cat :cofx ::coeffects
              :event ::parameterless-event)
 :ret ::effects
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
 navigated-to-exercise
 :args (s/cat :cofx ::coeffects
              :event ::navigated-to-exercise-event)
 :ret (s/and ::effects
             ::loader-shows-up
             (s/keys :req [::load-exercise])))
(defn navigated-to-exercise [{:keys [db]}
                             [_ exercise-id]]
  {:db (assoc-in db
                 [::db/ui
                  ::db/loader
                  ::db/visible]
                 true)
   ::load-exercise {::exercise-id exercise-id 
                    ::on-success [::navigated-to-exercise-success]
                    ::on-failure [::navigated-to-exercise-failure]}})

(rf/reg-event-fx
 ::navigated-to-exercise
 navigated-to-exercise)


(s/fdef 
 navigated-to-exercise-success
 :args (s/cat :cofx ::coeffects
              :event ::navigated-to-exercise-success-event)
 :ret (s/and ::effects
             ::loader-hides
             ::view-changes-to-exercise)
 :fn ::exercise-state-loads)
(defn navigated-to-exercise-success [{:keys [db]}
                                     [_ exercise]]
  {:db (-> db
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
                                            (::time exercise))))))})

(rf/reg-event-fx
 ::navigated-to-exercise-success
 navigated-to-exercise-success)


(s/fdef 
 navigated-to-exercise-failure
 :args (s/cat :cofx ::coeffects
              :event ::failure-event)
 :ret (s/and ::effects
             ::loader-hides))
(defn navigated-to-exercise-failure [{:keys [db]}
                                     [_ error]]
  {:db (-> db
           (assoc-in [::db/ui
                      ::db/loader
                      ::db/visible]
                     false))})

(rf/reg-event-fx
 ::navigated-to-exercise-failure
 navigated-to-exercise-failure)


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


(defn sign-in [{:keys [::credentials
                       ::on-success
                       ::on-failure]}]
  (js/setTimeout #(try (evt> (conj on-success
                                   credentials))
                       (catch js/Object ex
                         (evt> (conj on-failure
                                     ex))))
                 500))

(rf/reg-fx
 ::sign-in
 sign-in)
