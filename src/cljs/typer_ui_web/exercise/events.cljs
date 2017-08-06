(ns typer-ui-web.exercise.events
  (:require [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [typer-ui-web.db :as db]
            [typer-ui-web.exercise.db :as exercise-db]))


(s/def ::parameterless-event
  (s/tuple keyword?))


(s/def ::input-change-event
  (s/tuple keyword?))


(defn typed-character-gen-fn []
  gen/char-ascii)  


(s/def ::character-typed-event
  (s/tuple #{::character-typed}
           (s/with-gen char? typed-character-gen-fn)))


(s/def ::starts-exercise
  #(-> %
       (:ret)
       (::exercise-db/exercise)
       (::exercise-db/data)
       (::exercise-db/started)))


(s/def ::backspace-removes-last-character
  #(let [ch (-> %
               (:args)
               (:event)
               (second))
        in-text-actual (-> %
                           (:args)
                           (:db)
                           (::exercise-db/exercise)
                           (::exercise-db/data)                              
                           (::exercise-db/text)
                           (::exercise-db/actual))
        out-text-actual (-> %
                            (:ret)
                            (::exercise-db/exercise)
                            (::exercise-db/data)
                            (::exercise-db/text)
                            (::exercise-db/actual))]
     (if (= \backspace ch)
       (= out-text-actual
          (if (empty? in-text-actual)
            []
            (pop in-text-actual)))
       true)))


(s/def ::does-not-exceed-expected-text-length
  #(let [ch (-> %
               (:args)
               (:event)
               (second))
        in-text-actual (-> %
                           (:args)
                           (:db)
                           (::exercise-db/exercise)
                           (::exercise-db/data)                              
                           (::exercise-db/text)
                           (::exercise-db/actual))
         in-text-expected (-> %
                              (:args)
                              (:db)
                              (::exercise-db/exercise)
                              (::exercise-db/data)
                              (::exercise-db/text)
                              (::exercise-db/expected))
        out-text-actual (-> %
                            (:ret)
                            (::exercise-db/exercise)
                            (::exercise-db/data)
                            (::exercise-db/text)
                            (::exercise-db/actual))]
     (if (and (not= \backspace ch)
              (= (count in-text-actual)
                 (count in-text-expected)))
       (= out-text-actual in-text-actual)
       true)))


(s/def ::appends-new-character-after-last-character
  #(let [ch (-> %
               (:args)
               (:event)
               (second))
        in-text-actual (-> %
                           (:args)
                           (:db)
                           (::exercise-db/exercise)
                           (::exercise-db/data)
                           (::exercise-db/text)
                           (::exercise-db/actual))
         in-text-expected (-> %
                              (:args)
                              (:db)
                              (::exercise-db/exercise)
                              (::exercise-db/data)
                              (::exercise-db/text)
                              (::exercise-db/expected))
        out-text-actual (-> %
                            (:ret)
                            (::exercise-db/exercise)
                            (::exercise-db/data)
                            (::exercise-db/text)
                            (::exercise-db/actual))]
     (if (and (not= \backspace ch)
              (< (count in-text-actual)
                 (count in-text-expected)))
       (= out-text-actual (conj in-text-actual ch))
       true)))


(s/fdef 
 character-typed
 :args (s/cat :db ::db/db  
              :event ::character-typed-event) 
 :ret ::db/db
 :fn (s/and ::starts-exercise
            ::backspace-removes-last-character
            ::does-not-exceed-expected-text-length
            ::appends-new-character-after-last-character))
(defn character-typed [db [_ character]]
  (-> db
      (assoc-in [::exercise-db/exercise
                 ::exercise-db/data
                 ::exercise-db/started]
                true)
      (update-in [::exercise-db/exercise
                  ::exercise-db/data
                  ::exercise-db/text
                  ::exercise-db/actual]
                 #(cond 
                    (= \backspace character) (if (empty? %)
                                               %
                                               (pop %))
                    (= (count %)
                       (count (-> db
                                  (::exercise-db/exercise)
                                  (::exercise-db/data)
                                  (::exercise-db/text)
                                  (::exercise-db/expected)))) %
                    :else (conj % character)))))

(rf/reg-event-db
 ::character-typed
 character-typed)

