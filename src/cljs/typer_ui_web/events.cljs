(ns typer-ui-web.events
  (:require [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
            [typer-ui-web.db :as db]
            [typer-ui-web.exercise.db :as exercise-db]))


(s/def ::parameterless-event
  (s/tuple keyword?))


(s/def ::input-change-event
  (s/tuple keyword? string?))


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


(s/fdef 
 navigated-to-exercise
 :args (s/cat :db ::db/db  
              :event ::parameterless-event)
 :ret ::db/db
 :fn (s/and ::view-changes-to-exercise
            ::exercise-state-resets))
(defn navigated-to-exercise [db _]
  (-> db
      (assoc-in [::db/ui
                 ::db/view]
                ::db/exercise)
      (merge exercise-db/default-db)))

(rf/reg-event-db
 ::navigated-to-exercise
 navigated-to-exercise)



