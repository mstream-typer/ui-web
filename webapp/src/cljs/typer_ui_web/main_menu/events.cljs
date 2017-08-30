(ns typer-ui-web.main-menu.events
  (:require [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
            [typer-ui-web.common.core :refer [evt> <sub]]
            [typer-ui-web.common.events :as common-events]
            [typer-ui-web.common.db :as common-db]
            [typer-ui-web.main-menu.db :as db]))


(s/def ::input-change-event
  (s/tuple keyword?
           string?))


(s/def ::user-sign-in-requested-event
  (s/tuple keyword?
           ::credentials))


(s/def ::user-sign-in-succeed-event
  (s/tuple keyword?
           ::credentials))


(s/def ::sign-in
  (s/keys :req [::credentials
                ::on-success
                ::on-failure]))


(s/def ::credentials
  (s/keys :req [::db/username
                ::db/password]))


(s/def ::login-menu-visible
  (comp ::common-db/visible
        ::db/login-menu
        ::db/ui
        ::db/main-menu
        :db))


(s/def ::login-menu-not-visible
  (comp not
        ::common-db/visible
        ::db/login-menu
        ::db/ui
        ::db/main-menu
        :db))


(s/def ::login-menu-loader-visible
  (comp ::common-db/visible
        ::common-db/loader
        ::db/login-menu
        ::db/ui
        ::db/main-menu
        :db))


(s/def ::login-menu-loader-not-visible
  (comp not
        ::common-db/visible
        ::common-db/loader
        ::db/login-menu
        ::db/ui
        ::db/main-menu
        :db))


(s/def ::login-menu-inputs-clear
  #(let [login-menu (-> %
                        (:db)
                        (::db/main-menu)
                        (::db/ui)
                        (::db/login-menu))]
     (and (empty? (::db/username login-menu))
          (empty? (::db/password login-menu)))))


(s/def ::event-password-applied
  #(let [in-password (-> %
                         :args
                         :event
                         second)
         out-login-menu-password (-> %
                                     :ret
                                     :db
                                     ::db/main-menu
                                     ::db/ui
                                     ::db/login-menu
                                     ::db/password)]
     (= out-login-menu-password
        in-password)))
     

(s/def ::sets-credentials-from-event
  #(let [event-creds (-> %
                         (:args)
                         (:event)
                         (second))
         user-creds (-> %
                        (:ret)
                        (:db)
                        (::db/main-menu)
                        (::db/data)
                        (::db/user))]
     (= user-creds
        event-creds)))


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


(s/def ::user-dropdown-switched
  #(let [in-dropdown-visible (-> %
                                 (:args)
                                 (:cofx)
                                 (:db)
                                 (::db/main-menu)
                                 (::db/ui)
                                 (::db/user-dropdown)
                                 (::common-db/visible))
         out-dropdown-visible (-> %
                                  (:ret)
                                  (:db)
                                  (::db/main-menu) 
                                  (::db/ui)
                                  (::db/user-dropdown)
                                  (::common-db/visible))]
     (not= out-dropdown-visible
           in-dropdown-visible)))


(s/def ::resets-credentials
  #(= (-> db/default-db
          ::db/main-menu
          ::db/data
          ::db/user)
      (-> %
          (:db)
          (::db/main-menu)
          (::db/data)
          (::db/user))))


(s/fdef 
 user-signed-out
 :args (s/cat :cofx ::common-events/coeffects
              :event ::common-events/parameterless-event)
 :ret (s/and ::common-events/effects
             ::resets-credentials))
(defn user-signed-out [{:keys [db]}
                      _]
    {:db (-> db
             (assoc-in [::db/main-menu
                        ::db/data
                        ::db/user]
                       (-> db/default-db
                           ::db/main-menu
                           ::db/data
                           ::db/user)))})

(rf/reg-event-fx
 ::user-signed-out
 user-signed-out)


(s/fdef 
 user-dropdown-switched
 :args (s/cat :cofx ::common-events/coeffects  
              :event ::common-events/parameterless-event)
 :ret ::common-events/effects
 :fn ::user-dropdown-switched)
(defn user-dropdown-switched [{:keys [db]}
                              _] 
  {:db (-> db
           (update-in  [::db/main-menu
                        ::db/ui
                        ::db/user-dropdown
                        ::common-db/visible]
                     not))})

(rf/reg-event-fx
 ::user-dropdown-switched
 user-dropdown-switched)


(s/fdef 
 login-menu-button-pressed
 :args (s/cat :cofx ::common-events/coeffects
              :event ::common-events/parameterless-event)
 :ret (s/and ::common-events/effects
             ::login-menu-visible))
(defn login-menu-button-pressed [{:keys [db]}
                                 [_ character]] 
  {:db (-> db
           (assoc-in [::db/main-menu
                      ::db/ui
                      ::db/login-menu
                      ::common-db/visible]
                     true))})

(rf/reg-event-fx
 ::login-menu-button-pressed
 login-menu-button-pressed)


(s/fdef 
 user-sign-in-requested
 :args (s/cat :cofx ::common-events/coeffects  
              :event ::user-sign-in-requested-event)
 :ret (s/and ::common-events/effects
             ::login-menu-visible
             ::login-menu-loader-visible
             (s/keys :req [::sign-in]))
 :fn ::causes-sign-in-effect)
(defn user-sign-in-requested [{:keys [db]}
                              [_ creds]]
  {:db (-> db
           (assoc-in [::db/main-menu
                      ::db/ui
                      ::db/login-menu
                      ::common-db/visible]
                     true)
           (assoc-in [::db/main-menu
                      ::db/ui
                      ::db/login-menu
                      ::common-db/loader
                      ::common-db/visible]
                     true))
   ::sign-in {::credentials creds
              ::on-success [::user-sign-in-succeed]
              ::on-failure [::user-sign-in-failed]}})

(rf/reg-event-fx
 ::user-sign-in-requested
 user-sign-in-requested)


(s/fdef 
 user-sign-in-succeed
 :args (s/cat :cofx ::common-events/coeffects
              :event ::user-sign-in-succeed-event)
 :ret (s/and ::common-events/effects
             ::login-menu-not-visible
             ::login-menu-loader-not-visible)
 :fn ::sets-credentials-from-event)
(defn user-sign-in-succeed [{:keys [db]}
                            [_ credentials]]
  {:db (-> db 
           (assoc-in [::db/main-menu
                      ::db/ui
                      ::db/login-menu
                      ::common-db/visible]
                     false)
           (assoc-in [::db/main-menu
                      ::db/ui
                      ::db/login-menu
                      ::common-db/loader
                      ::common-db/visible]
                     false)
           (assoc-in [::db/main-menu
                      ::db/data
                      ::db/user]
                     credentials))})

(rf/reg-event-fx
 ::user-sign-in-succeed
 user-sign-in-succeed)


(s/fdef 
 user-sign-in-failed
 :args (s/cat :cofx ::common-events/coeffects
              :event ::common-events/failure-event)
 :ret (s/and ::common-events/effects
             ::login-menu-not-visible
             ::login-menu-loader-not-visible))
(defn user-sign-in-failed [{:keys [db]}
                           _] 
  {:db (-> db
           (assoc-in [::db/main-menu
                      ::db/ui
                      ::db/login-menu
                      ::common-db/visible]
                     false)
           (assoc-in [::db/main-menu
                      ::db/ui
                      ::db/login-menu
                      ::common-db/loader
                      ::common-db/visible]
                     false))})

(rf/reg-event-fx
 ::user-sign-in-failed
 user-sign-in-failed)


(s/fdef
 login-menu-username-changed
 :args (s/cat :cofx ::common-events/coeffects
              :event ::input-change-event)
 :ret ::common-events/effects
 :fn #(let [in-username (-> %
                            :args
                            :event
                            second)
            out-login-menu-username (-> %
                                        :ret
                                        :db
                                        ::db/main-menu
                                        ::db/ui
                                        ::db/login-menu
                                        ::db/username)]
        (= out-login-menu-username in-username)))
(defn login-menu-username-changed [{:keys [db]}
                                   [_ username]]
  {:db (-> db
           (assoc-in [::db/main-menu
                      ::db/ui
                      ::db/login-menu
                      ::db/username] username))})

(rf/reg-event-fx
 ::login-menu-username-changed
 login-menu-username-changed)


(s/fdef 
 login-menu-password-changed
 :args (s/cat :cofx ::common-events/coeffects
              :event ::input-change-event)
 :ret ::common-events/effects
 :fn ::event-password-applied)
(defn login-menu-password-changed [{:keys [db]}
                                   [_ password]]
  {:db (-> db
           (assoc-in [::db/main-menu
                      ::db/ui
                      ::db/login-menu
                      ::db/password]
                     password))})

(rf/reg-event-fx
 ::login-menu-password-changed
 login-menu-password-changed)


(s/fdef 
 cancel-login-menu-button-pressed
 :args (s/cat :cofx ::common-events/coeffects
              :event ::common-events/parameterless-event)
 :ret (s/and ::common-events/effects
             ::login-menu-not-visible))
(defn cancel-login-menu-button-pressed [{:keys [db]}
                                        _] 
  {:db (-> db
           (assoc-in [::db/main-menu
                      ::db/ui
                      ::db/login-menu
                      ::common-db/visible]
                     false))})

(rf/reg-event-fx
 ::cancel-login-menu-button-pressed
 cancel-login-menu-button-pressed)


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
