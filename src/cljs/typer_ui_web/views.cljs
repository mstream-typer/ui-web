(ns typer-ui-web.views
  (:require [typer-ui-web.db :as db]
            [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]))


(s/def ::sheet-size
  (s/and int? #(< 0 %)))


(s/def ::sheet-width
  ::sheet-size)


(s/def ::sheet-height
  ::sheet-size)


(s/def ::result
  #{:success :failure})


(s/def ::value
  (s/coll-of ::db/exercise-text))


(s/def ::keeps-same-length
  #(let [in-text (-> % :args :text)
         result (-> % :ret ::result)
         out-text (-> % :ret ::value)]
     (if (= result :failure)
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
  #(let [in-text (-> % :args :text)
         out-text (-> % :ret ::value)
         result (-> % :ret ::result)]
     (if (= result :failure)
       (nil? out-text)
       (every? (partial s/valid? ::newline-last-if-present) out-text))))


(s/def ::segment
  (s/and ::db/exercise-text
         #(or (s/valid? ::new-line-present %)
              (s/valid? ::space-present %))))


(s/fdef
 words-fit-sheet-width?
 :args (s/cat :text ::db/exercise-text
              :sheet-width ::sheet-width)
 :ret boolean?
 :fn #(let [in-text (-> % :args :text)
            sheet-width (-> % :args :sheet-width)
            result (% :ret)]
        (= result 
           (->> (partition-by (partial s/valid? ::db/whitespace) in-text)
                (filter (complement (comp (partial s/valid? ::db/whitespace)
                                          first)))
                (map count)
                (cons 0)
                (apply max)
                (> sheet-width)))))
(def words-fit-sheet-width?
  (memoize
   (fn [text sheet-width]
     (->> (partition-by (partial s/valid? ::db/whitespace) text)
          (filter (complement (comp (partial s/valid? ::db/whitespace)
                                    first)))
          (map count)
          (cons 0)
          (apply max)
          (> sheet-width)))))


(s/def ::fails-on-too-long-input
  #(let [in-text (-> % :args :text)
         sheet-width (-> % :args :sheet-width)
         result (-> % :ret ::result)]
     (= result (if (words-fit-sheet-width? in-text sheet-width)
                 :success
                 :failure))))


(s/fdef
 next-row-index
 :args (s/cat :text ::segment)
 :ret int?
 :fn #(let [in-text (-> % :args :text)
            out-index (% :ret)]
        (if (s/valid? ::new-line-present in-text)
          (= out-index (inc (count (take-while (partial not= \newline)
                                               in-text))))
          (= out-index (count (drop-while (partial not= \space)
                                          (reverse in-text)))))))
(def next-row-index
  (memoize
   (fn [text]
     (if (s/valid? ::new-line-present text)
       (inc (count (take-while (partial not= \newline) text)))
       (count (drop-while (partial not= \space) (reverse text)))))))


(s/fdef
 format-text
 :args (s/cat :text ::db/exercise-text  
              :sheet-width ::sheet-width)
 :ret (s/keys :req [::result]
              :opt [::value])
 :fn (s/and ::keeps-same-length
            ::fails-on-too-long-input
            ::breaks-new-lines))
(def format-text
  (memoize
   (fn [text sheet-width]
     (if (not (words-fit-sheet-width? text sheet-width))
       {::result :failure}
       (loop [result []
              remaining text]
         (let [[next-row other-rows] (split-at (next-row-index (take sheet-width
                                                                     remaining))
                                               remaining)
               next-result (conj result next-row)]
           (cond
             (empty? next-row) {::result :success ::value (conj result other-rows)}
             (empty? other-rows) {::result :success ::value (conj result next-row)}
             :else (recur next-result other-rows))))))))
  
  
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


(s/fdef 
 character-class
 :args (s/cat :text-expected ::db/exercise-text
              :text-actual string?
              :character-index (s/and int? pos?))
 :ret string?
 :fn #(let [text-expected (-> % :args :text-expected)
            text-actual (-> % :args :text-actual)
            char-index (-> % :args :character-index)
            result (% :ret)
            ch-actual (get text-actual char-index)
            ch-expected (get text-expected char-index)]
        (if (> (count text-actual) (count text-expected))
          ""
          (and (str/includes? result (if (s/valid? ::db/whitespace ch-expected)
                                       "whitespace"
                                       "character"))
               (str/includes? result (if ch-actual
                                       "typed"
                                       "untyped"))
               ((if (and ch-actual (not= ch-actual ch-expected))
                  identity
                  not) (str/includes? result "incorrect"))
               ((if (and ch-actual
                         (not= (take char-index text-actual)
                               (take char-index text-expected)))
                  identity
                  not) (str/includes? result "after-incorrect"))
               ((if (= char-index (count text-actual))
                  identity 
                  not) (str/includes? result "cursor"))))))
(def character-class
  (memoize
   (fn [text-expected text-actual character-index]
     (let [ch-actual (get text-actual character-index)
           ch-expected (get text-expected character-index)]
       (if (> (count text-actual) (count text-expected))
         ""
         (str/join \space [(str (if (s/valid? ::db/whitespace ch-expected)
                                  "whitespace"
                                  "character")
                                "-"
                                (if ch-actual
                                  "typed"
                                  "untyped"))
                           (when (and ch-actual
                                      (not= ch-actual ch-expected)) "incorrect")
                           (when (and ch-actual
                                      (not= (take character-index text-actual)
                                            (take character-index text-expected))) "after-incorrect")
                           (when (= character-index
                                    (count text-actual)) "cursor")]))))))


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


(defn main-panel []
  (let [sheet-width 20
        sheet-height 5
        text-expected @(rf/subscribe [:exercise-text-expected])
        text-actual @(rf/subscribe [:exercise-text-actual])
        formatted-text (-> text-expected
                           (format-text sheet-width)
                           ::value
                           index-formatted-text)
        whitespace-symbols {\space \u23b5
                            \newline \u21b5}
        current-line-idx (current-line formatted-text
                                       (dec (count text-actual)))
        sheet-middle (quot (dec sheet-height) 2)]
    [:div#exercise.ui.raised.segment
     (for [idx (range sheet-height)]
       (let [line-idx (- (+ current-line-idx idx) sheet-middle)]
         (cond
           (neg? line-idx)
           ^{:key line-idx}
           [:span.line]
           (>= line-idx (count formatted-text))
           ^{:key line-idx}
           [:span.line]
           :else
           ^{:key line-idx}
           [:span.line
            (for [[ch-idx ch] (second (formatted-text line-idx))]
              ^{:key ch-idx}
              [:span {:class (character-class text-expected
                                              text-actual
                                              ch-idx)}
               (whitespace-symbols ch ch)])])))])) 
