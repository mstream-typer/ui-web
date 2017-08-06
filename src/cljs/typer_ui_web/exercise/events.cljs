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


(s/def ::finishes-exercise-when-whole-text-matches
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
         out-exercise-finished (-> %
                                   (:ret)
                                   (::exercise-db/exercise)
                                   (::exercise-db/finished))]
     (if (= in-text-expected
            (conj in-text-actual ch))
       out-exercise-finished
       true)))


(s/def ::ignores-typing-after-exercise-is-finished
  #(let [in-text-actual (-> %
                            (:args)
                            (:db)
                            (::exercise-db/exercise)
                            (::exercise-db/data)
                            (::exercise-db/text)
                            (::exercise-db/actual))
         in-finished (-> %
                         (:args)
                         (:db)
                         (::exercise-db/exercise)
                         (::exercise-db/data)
                         (::exercise-db/finished))
         out-text-actual (-> %
                             (:ret)
                             (::exercise-db/exercise)
                             (::exercise-db/data)
                             (::exercise-db/text)
                             (::exercise-db/actual))]
     (if in-finished
       (= out-text-actual in-text-actual)
       true)))


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
         in-finished (-> %
                         (:args)
                         (:db)
                         (::exercise-db/exercise)
                         (::exercise-db/data)
                         (::exercise-db/finished))
        out-text-actual (-> %
                            (:ret)
                            (::exercise-db/exercise)
                            (::exercise-db/data)
                            (::exercise-db/text)
                            (::exercise-db/actual))]
     (if (and (not in-finished)
              (not= \backspace ch)
              (< (count in-text-actual)
                 (count in-text-expected)))
       (= out-text-actual (conj in-text-actual ch))
       true)))


(s/def ::finishes-exercise-when-whole-text-matches
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
         in-finished (-> %
                         (:args)
                         (:db)
                         (::exercise-db/exercise)
                         (::exercise-db/data)
                         (::exercise-db/finished))
         out-exercise-finished (-> %
                                   (:ret)
                                   (::exercise-db/exercise)
                                   (::exercise-db/finished))]
     (if (and (not in-finished)
              (= in-text-expected
                 (conj in-text-actual ch)))
       out-exercise-finished
       true)))


(s/fdef 
 character-typed
 :args (s/cat :db ::db/db 
              :event ::character-typed-event) 
 :ret ::db/db
 :fn (s/and ::starts-exercise
            ::finishes-exercise-when-whole-text-matches
            ::ignores-typing-after-exercise-is-finished
            ::backspace-removes-last-character
            ::does-not-exceed-expected-text-length
            ::appends-new-character-after-last-character))
(defn character-typed [db [_ character]]
  (let [exercise-finished (-> db
                              (::exercise-db/exercise)
                              (::exercise-db/data)
                              (::exercise-db/finished))
        text-expected (-> db
                          (::exercise-db/exercise)
                          (::exercise-db/data)
                          (::exercise-db/text)
                          (::exercise-db/expected))
        text-actual (-> db
                        (::exercise-db/exercise)
                        (::exercise-db/data)
                        (::exercise-db/text)
                        (::exercise-db/actual))
        next-text-actual (cond 
                           (= \backspace character) (if (empty? text-actual)
                                                      text-actual
                                                      (pop text-actual))
                           (= (count text-actual)
                              (count text-expected)) text-actual
                           :else (conj text-actual character))]
    (if exercise-finished
      db
      (-> db
          (assoc-in [::exercise-db/exercise
                     ::exercise-db/data
                     ::exercise-db/started]
                    true)
          (assoc-in [::exercise-db/exercise
                     ::exercise-db/data
                     ::exercise-db/text
                     ::exercise-db/actual]
                    next-text-actual)
          (assoc-in [::exercise-db/exercise
                     ::exercise-db/data 
                     ::exercise-db/finished]
                    (= next-text-actual text-expected))))))

(rf/reg-event-db
 ::character-typed
 character-typed)


(s/def ::exercise-timer-counts-down
  #(let [in-started (-> %
                        (:args)
                        (:db)
                        (::exercise-db/exercise)
                        (::exercise-db/data)
                        (::exercise-db/started))
         in-finished (-> %
                         (:args)
                         (:db)
                         (::exercise-db/exercise)
                         (::exercise-db/data)
                         (::exercise-db/finished))
         in-timer (-> %
                      (:args)
                      (:db)
                      (::exercise-db/exercise)
                      (::exercise-db/data)
                      (::exercise-db/timer)
                      (::exercise-db/current))
         out-timer (-> %
                      (:ret)
                      (::exercise-db/exercise)
                      (::exercise-db/data)
                      (::exercise-db/timer)
                      (::exercise-db/current))]
     (if (and in-started
              (not in-finished)
              (pos? in-timer))
         (= out-timer (dec in-timer))
         true)))


(s/def ::exercise-timer-does-not-go-below-zero
  #(let [out-timer (-> %
                       (:ret)
                       (::exercise-db/exercise)
                       (::exercise-db/data)
                       (::exercise-db/timer)
                       (::exercise-db/current))]
     (not (neg? out-timer))))


(s/def ::finishes-exercise-when-reaches-zero
  #(let [in-timer (-> %
                      (:args)
                      (:db)
                      (::exercise-db/exercise)
                      (::exercise-db/data)
                      (::exercise-db/timer)
                      (::exercise-db/current))
         in-started (-> %
                        (:args)
                        (:db)
                        (::exercise-db/exercise)
                        (::exercise-db/data)
                        (::exercise-db/started))
         out-finished (-> %
                          (:ret)
                          (::exercise-db/exercise)
                          (::exercise-db/data)
                          (::exercise-db/finished))]
     (if (and in-started
              (zero? in-timer))  
       out-finished
       true)))


(s/def ::tick-ignored-if-exercise-not-started
  #(let [in-started (-> %
                        (:args)
                        (:db)
                        (::exercise-db/exercise)
                        (::exercise-db/data)
                        (::exercise-db/started))
         in-timer (-> %
                      (:args)
                      (:db)
                      (::exercise-db/exercise)
                      (::exercise-db/data)
                      (::exercise-db/timer)
                      (::exercise-db/current))
         out-timer (-> %
                      (:ret)
                      (::exercise-db/exercise)
                      (::exercise-db/data)
                      (::exercise-db/timer)
                      (::exercise-db/current))]
       (if (not in-started)
         (= out-timer in-timer)
         true)))


(s/def ::tick-ignored-if-exercise-finished
  #(let [in-finished (-> %
                      (:args)
                      (:db)
                      (::exercise-db/exercise)
                      (::exercise-db/data)
                      (::exercise-db/finished))
         in-timer (-> %
                      (:args)
                      (:db)
                      (::exercise-db/exercise)
                      (::exercise-db/data)
                      (::exercise-db/timer)
                      (::exercise-db/current))
         out-timer (-> %
                      (:ret)
                      (::exercise-db/exercise)
                      (::exercise-db/data)
                      (::exercise-db/timer)
                      (::exercise-db/current))]
     (if in-finished
       (= out-timer in-timer)
       true)))


(s/fdef 
 timer-ticked
 :args (s/cat :db ::db/db  
              :event ::parameterless-event) 
 :ret ::db/db
 :fn (s/and ::exercise-timer-counts-down
            ::exercise-timer-does-not-go-below-zero
            ::finishes-exercise-when-reaches-zero
            ::tick-ignored-if-exercise-not-started
            ::tick-ignored-if-exercise-finished))
(defn timer-ticked [db [_ _]]
  (let [exercise-started (-> db
                             (::exercise-db/exercise)
                             (::exercise-db/data)
                             (::exercise-db/started))
        exercise-finished (-> db
                             (::exercise-db/exercise)
                             (::exercise-db/data)
                             (::exercise-db/finished))
        timer (-> db
                  (::exercise-db/exercise)
                  (::exercise-db/data)
                  (::exercise-db/timer)
                  (::exercise-db/current))]
    (if (or (not exercise-started)
            exercise-finished)
      db
      (-> db
          (assoc-in [::exercise-db/exercise
                     ::exercise-db/data
                     ::exercise-db/timer
                     ::exercise-db/current]
                    (if (zero? timer)
                      0
                      (dec timer)))
          (assoc-in [::exercise-db/exercise
                     ::exercise-db/data
                     ::exercise-db/finished]
                    (zero? timer))))))

(rf/reg-event-db
 ::timer-ticked
 timer-ticked)

       
