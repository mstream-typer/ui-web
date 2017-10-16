(ns typer-ui-web.exercise.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [typer-ui-web.exercise.db :as db]
            [typer-ui-web.common.db :as common-db]
            [clojure.spec.alpha :as s]
            [re-frame.core :as rf]))


(s/def ::result
  #{::success
    ::failure})


(s/def ::value
  (s/coll-of ::db/exercise-text))


(s/def ::keeps-same-length
  #(let [in-text (-> %
                     (:args)
                     (:text))
         result (-> %
                    (:ret)
                    (::result))
         out-text (-> %
                      (:ret)
                      (::value))]
     (if (= result ::failure)
       (nil? out-text)
       (= (count in-text) (count (flatten out-text))))))


(s/def ::new-line-present
  #(some (partial = \newline) %))


(s/def ::space-present
  #(some (partial = \space) %))


(s/def ::newline-last-if-present
  #(or (not (s/valid? ::new-line-present %))
       (= \newline (last %))))


(s/def ::breaks-new-lines
  #(let [in-text (-> %
                     (:args)
                     (:text))
         out-text (-> %
                      (:ret)
                      (::value))
         result (-> %
                    (:ret)
                    (::result))]
     (if (= result ::failure)
       (nil? out-text)
       (every? (partial s/valid?
                        ::newline-last-if-present)
               out-text))))


(s/def ::segment
  (s/and ::db/exercise-text
         #(or (s/valid? ::new-line-present %)
              (s/valid? ::space-present %))))


(s/fdef
 words-fit-sheet-width?
 :args (s/cat :text ::db/exercise-text
              :sheet-width ::db/width)
 :ret boolean?
 :fn #(let [in-text (-> %
                        (:args)
                        (:text))
            sheet-width (-> %
                            (:args)
                            (:sheet-width))
            result (% :ret)]
        (= result
           (->> (partition-by (partial s/valid?
                                       ::db/whitespace)
                              in-text)
                (filter (complement (comp (partial s/valid?
                                                   ::db/whitespace)
                                          first)))
                (map count)
                (cons 0)
                (apply max)
                (> sheet-width)))))
(def words-fit-sheet-width?
  (memoize
   (fn [text sheet-width]
     (->> (partition-by (partial s/valid?
                                 ::db/whitespace)
                        text)
          (filter (complement (comp (partial s/valid?
                                             ::db/whitespace)
                                    first)))
          (map count)
          (cons 0)
          (apply max)
          (> sheet-width)))))


(s/def ::fails-on-too-long-input
  #(let [in-text (-> %
                     (:args)
                     (:text))
         sheet-width (-> %
                         (:args)
                         (:sheet-width))
         result (-> %
                    (:ret)
                    (::result))]
     (if-not (words-fit-sheet-width? in-text
                                     sheet-width)
       (and (= result ::failure)
            (-> %
                (:ret)
                (::error)))
       true)))


(s/fdef
 next-row-index
 :args (s/cat :text ::segment)
 :ret int?
 :fn #(let [in-text (-> %
                        (:args)
                        (:text))
            out-index (:ret %)]
        (if (s/valid? ::new-line-present in-text)
          (= out-index (inc (count (take-while (partial not=
                                                        \newline)
                                               in-text))))
          (= out-index (count (drop-while (partial not=
                                                   \space)
                                          (reverse in-text)))))))
(def next-row-index
  (memoize
   (fn [text]
     (if (s/valid? ::new-line-present text)
       (inc (count (take-while (partial not=
                                        \newline)
                               text)))
       (count (drop-while (partial not=
                                   \space)
                          (reverse text)))))))


(defn index-formatted-text
  ([text] (index-formatted-text text [] 0))
  ([remaining result len]
   (if (empty? remaining)
     result
     (let [row (first remaining)]
       (recur (rest remaining)
              (conj result [(count result)
                            (map-indexed (fn [idx v] [(+ len idx) v])
                                         row)])
              (+ len (count row)))))))


(defn current-line
  ([formatted-text character-index]
   (current-line formatted-text character-index 0))
  ([remaining-rows character-index characters-count]
   (let [[row-index row] (first remaining-rows)
         next-chars-count (+ characters-count (count row))
         next-remaining-rows (rest remaining-rows)]
     (if (or
          (empty? next-remaining-rows)
          (< (inc character-index) next-chars-count))
       row-index
       (recur next-remaining-rows
              character-index
              next-chars-count)))))


(s/def ::error
  string?)


(s/fdef
 format-text
 :args (s/cat :text ::db/exercise-text
              :sheet-width ::db/width)
 :ret (s/keys :req [::result]
              :opt [::value ::error])
 :fn (s/and ::keeps-same-length
            ::fails-on-too-long-input
            ::breaks-new-lines))
(def format-text
  (memoize
   (fn [text sheet-width]
     (if-not (words-fit-sheet-width? text
                                     sheet-width)
       {::result ::failure
        ::error "words don't fit the sheet width"}
       (loop [result []
              remaining text]
         (let [[next-row other-rows] (split-at (next-row-index (take sheet-width
                                                                     remaining))
                                               remaining)
               next-result (conj result (vec next-row))]
           (cond
             (empty? next-row) {::result ::success
                                ::value (conj result (vec other-rows))}
             (empty? other-rows) {::result ::success
                                  ::value next-result}
             :else (recur next-result (vec other-rows)))))))))


(rf/reg-sub
 ::loading?
 (comp ::common-db/visible
       ::common-db/loader
       ::db/ui
       ::db/execise))

(rf/reg-sub
 ::exercise-text-actual
 (comp ::db/actual
       ::db/text
       ::db/data
       ::db/exercise))


(rf/reg-sub
 ::exercise-text-expected
 (comp ::db/expected
       ::db/text
       ::db/data
       ::db/exercise))


(rf/reg-sub
 ::exercise-sheet-height
 (comp ::db/height
       ::db/sheet
       ::db/ui
       ::db/exercise))


(rf/reg-sub
 ::exercise-sheet-width
 (comp ::db/width
       ::db/sheet
       ::db/ui
       ::db/exercise))


(rf/reg-sub
 ::exercise-started
 (comp ::db/started
       ::db/data
       ::db/exercise))


(rf/reg-sub
 ::exercise-finished
 (comp ::db/finished
       ::db/data
       ::db/exercise))


(rf/reg-sub
 ::exercise-timer-initial
 (comp ::db/initial
       ::db/timer
       ::db/data
       ::db/exercise))


(rf/reg-sub
 ::exercise-timer-current
 (comp ::db/current
       ::db/timer
       ::db/data
       ::db/exercise))


(rf/reg-sub
 ::summary-modal-open
 (comp ::common-db/visible
       ::db/summary-modal
       ::db/ui
       ::db/exercise))


(rf/reg-sub
 ::exercise-timer-current-formatted
 :<- [::exercise-timer-current]
 (fn [current _]
   (let [minutes (quot current 60)
         seconds (mod current 60)]
     (str (if (< minutes 10)
            (str "0" minutes)
            minutes)
          ":"
          (if (< seconds 10)
            (str "0" seconds)
            seconds)))))


(rf/reg-sub
 ::exercise-progress
 :<- [::exercise-text-actual]
 :<- [::exercise-text-expected]
 (fn [[text-actual text-expected] _]
   (-> (count text-actual)
       (* 100)
       (/ (count text-expected))
       (str "%"))))


(rf/reg-sub
 ::exercise-current-line
 :<- [::exercise-text-formatted]
 :<- [::exercise-text-actual]
 (fn [[formatted-text actual-text] _]
   (current-line formatted-text (dec (count actual-text)))))


(rf/reg-sub
 ::exercise-text-formatted
 :<- [::exercise-text-expected]
 :<- [::exercise-sheet-width]
 (fn [[text-expected exercise-sheet-width] _]
   (-> text-expected
       (format-text exercise-sheet-width)
       ::value
       index-formatted-text)))


(rf/reg-sub
 ::modal-open
 :<- [::summary-modal-open]
 (fn [summary-modal-open _]
   summary-modal-open))


(rf/reg-sub
 ::summary-modal-message
 :<- [::summary-modal-open]
 :<- [::exercise-finished]
 :<- [::exercise-timer-initial]
 :<- [::exercise-timer-current]
 :<- [::exercise-text-actual]
 :<- [::exercise-text-expected]
 (fn [[summary-modal-open
       finished
       timer-initial
       timer-current
       text-actual
       text-expected]
      _]
   (cond

     (or (not summary-modal-open)
         (not finished))
     ""

     (zero? timer-current)
     (let [exercise-time (- timer-initial timer-current)
           text-length (->> (map = text-expected text-actual)
                            (filter true?)
                            (count))]
       (str "You failed! Your speed was "
            (int (/ (* 60 text-length)
                    exercise-time))
            " c/m."))

     :else (let [exercise-time (- timer-initial
                                  timer-current)
                 text-length (count text-expected)]
             (str "You succeed! Your speed was "
                  (int (/ (* 60 text-length)
                          exercise-time))
                  " c/m.")))))
