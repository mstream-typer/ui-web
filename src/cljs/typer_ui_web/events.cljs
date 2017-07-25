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


(rf/reg-event-db :character-typed character-typed)











