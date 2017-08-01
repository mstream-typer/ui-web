(ns typer-ui-web.events
  (:require [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
            [typer-ui-web.db :as db]))

(def dummy-text
  [\x \a \a \a \a \a \a \space \a \a \a \a \a \a \newline
   \b \b \b \b \b \b \b \b \b \b \b \b \space \b \b \b \b \b \b \b \space \b \b \b \b \newline
   \c \c \c \c \c \c \c \c \c \c \c \c \c \space \c \c \c \c \c
   \d \d \d \space \d \d \d \space \d \d \d \space \d \d \d \newline
   \e \e \e \e \e \e \e \e \e \e])


(rf/reg-event-db
 :db-initialized
 (fn  [_ _]
   db/default-db))


(rf/reg-event-db
 :exercise-loaded
 (fn [db _]
   (-> db
       (assoc-in [::db/exercise ::db/text ::db/expected]
                 dummy-text)
       (assoc-in [::db/exercise ::db/text ::db/actual]
                 []))))


(s/def ::parameterless-event
  (s/tuple keyword?))


(s/def ::input-change-event
  (s/tuple keyword?))


(s/def ::character-typed
  (s/tuple keyword? (s/and char?)))


(s/fdef 
 character-typed
 :args (s/cat :db ::db/db  
              :event ::character-typed)
 :ret ::db/db
 :fn #(let [in-text-actual (-> % :args :db ::db/exercise ::db/text ::db/actual)
            text-expected (-> % :args :db ::db/exercise ::db/text ::db/expected)
            ch (-> % :args :event second)
            out-text-actual (-> % :ret ::db/exercise ::db/text ::db/actual)]
        (cond
          (= \backspace ch) (= out-text-actual
                               (if (empty? in-text-actual)
                                 []
                                 (pop in-text-actual)))
          (= (count in-text-actual)
             (count text-expected)) (= out-text-actual in-text-actual)
          :else (= out-text-actual (conj in-text-actual ch)))))
(defn character-typed [db [_ character]]
  (-> db
      (update-in [::db/exercise ::db/text ::db/actual]
                 #(cond 
                    (= \backspace character) (if (empty? %)
                                               %
                                               (pop %))
                    (= (count %) (count (-> db
                                            ::db/exercise
                                            ::db/text
                                            ::db/expected))) %
                    :else (conj % character)))))


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


(s/fdef 
 navigated-to-exercise
 :args (s/cat :db ::db/db  
              :event ::parameterless-event)
 :ret ::db/db
 :fn #(let [out-view (-> %
                         :ret
                         ::db/ui
                         ::db/view)]
        (= out-view :exercise)))
(defn navigated-to-exercise [db _]
  (-> db
      (assoc-in [::db/ui ::db/view] :exercise)))


(s/fdef 
 navigated-to-home
 :args (s/cat :db ::db/db  
              :event ::parameterless-event)
 :ret ::db/db
 :fn #(let [out-view (-> %
                         :ret
                         ::db/ui
                         ::db/view)]
        (= out-view :home)))
(defn navigated-to-home [db _]
  (-> db
      (assoc-in [::db/ui ::db/view] :home)))

 
(rf/reg-event-db :navigated-to-home navigated-to-home)


(rf/reg-event-db :navigated-to-exercise navigated-to-exercise)


(rf/reg-event-db :character-typed character-typed)


(rf/reg-event-db :login-menu-button-pressed login-menu-button-pressed)


(rf/reg-event-db :cancel-login-menu-button-pressed cancel-login-menu-button-pressed)


(rf/reg-event-db :login-menu-username-changed login-menu-username-changed)


(rf/reg-event-db :login-menu-password-changed login-menu-password-changed)










