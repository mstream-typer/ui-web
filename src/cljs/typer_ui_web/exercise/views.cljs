(ns typer-ui-web.exercise.views
  (:require [typer-ui-web.exercise.db :as exercise-db]
            [typer-ui-web.events :as events]
            [typer-ui-web.exercise.events :as exercise-events]
            [typer-ui-web.exercise.subs :as exercise-subs]
            [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [clojure.string :as str]))


(s/def ::no-class-for-letters-beyond-expected
  #(let [char-idx (-> % :args :character-index)
         text-expected (-> % :args :text-expected)
         result (% :ret)] 
     ((if (>= char-idx
              (count text-expected))
        identity
        not) (empty? result)))) 


(s/def ::whitespace-or-character-exclusively
  #(let [char-idx (-> % :args :character-index) 
         whitespace? (s/valid? ::exercise-db/whitespace
                               (-> %
                                   :args
                                   :text-expected
                                   (get char-idx)))
         result (% :ret)]
     (and (re-find (if whitespace? 
                     #"(?: |^)whitespace-\w+"
                     #"(?: |^)character-\w+")
                   result)
          (not (re-find (if whitespace?
                          #"(?: |^)character-\w+"
                          #"(?: |^)whitespace-\w+")
                        result)))))


(s/def ::typed-or-untyped-exclusively
  #(let [char-idx (-> % :args :character-index) 
         typed? (-> %
                    :args
                    :text-actual
                    (get char-idx)
                    some?) 
         result (% :ret)]
     (and (re-find (if typed?
                     #"\w+-typed(?: |$)"
                     #"\w+-untyped(?: |$)")
                   result)
          (not (re-find (if typed?
                          #"\w+-untyped(?: |$)"
                          #"\w+-typed(?: |$)")
                        result))))) 


(s/def ::right-positioned-cursor
  #(let [char-idx (-> % :args :character-index) 
         typed-chars-count (-> %
                               :args
                               :text-actual
                               count)
         result (% :ret)]
     ((if (= char-idx
             typed-chars-count)
        identity
        not) (re-find #"(?: |^)cursor(?: |$)"
                      result))))


(s/def ::marks-mistakes
  #(let [char-idx (-> % :args :character-index)
         actual-char (-> %
                         :args
                         :text-actual
                         (get char-idx))
         expected-char (-> %
                           :args
                           :text-expected
                           (get char-idx))
         actual-text-before-cursor (->> %
                                        :args
                                        :text-actual
                                        (take char-idx))
         expected-text-before-cursor (->> %
                                          :args
                                          :text-expected
                                          (take char-idx))
         result (% :ret)]
     ((if (and (some? actual-char)
               (not= actual-char 
                     expected-char)
               (= actual-text-before-cursor
                  expected-text-before-cursor))
        identity 
        not) (re-find #"(?: |^)incorrect(?: |$)"
                      result))))


(s/def ::marks-characters-after-first-mistake
  #(let [char-idx (-> % :args :character-index)
         actual-text-before-cursor (->> %
                                        :args
                                        :text-actual
                                        (take char-idx))
         expected-text-before-cursor (->> %
                                          :args
                                          :text-expected
                                          (take char-idx))
         result (% :ret)]
     ((if (and (-> % :args :text-actual (get char-idx) some?)
               (not= actual-text-before-cursor
                     expected-text-before-cursor))
        identity
        not) (re-find #"(?: |^)after-incorrect(?: |$)"
                      result))))


(defn character-index-gen-fn []
  (gen/resize 10 gen/pos-int))


(defn actual-text-gen-fn []
  (gen/vector gen/char-ascii 0 10))


(s/def ::text-actual
  (s/with-gen (s/coll-of char?) actual-text-gen-fn))


(s/def ::character-index
  (s/with-gen (s/and int? pos?) character-index-gen-fn))


(s/fdef
 character-class
 :args (s/and (s/cat :text-expected ::exercise-db/exercise-text
                     :text-actual ::text-actual
                     :character-index ::character-index)
              #(< (% :character-index) (-> % :text-expected count))
              #(<= (% :text-actual count) (% :text-expected count)))
 :ret string?
 :fn (s/and ::no-class-for-letters-beyond-expected
            ::whitespace-or-character-exclusively
            ::typed-or-untyped-exclusively 
            ::right-positioned-cursor
            ::marks-mistakes
            ::marks-characters-after-first-mistake))
(def character-class
  (memoize
   (fn [text-expected text-actual character-index]
     (let [ch-actual (get text-actual character-index)
           ch-expected (get text-expected character-index)
           text-before-cursor-matches? (= (take character-index text-actual)
                                          (take character-index text-expected))]
       (if (>= character-index (count text-expected)) 
         "" 
         (str/join \space
                   [(str (if (s/valid? ::exercise-db/whitespace
                                       ch-expected)
                           "whitespace"
                           "character")
                         "-"
                         (if ch-actual
                           "typed"
                           "untyped"))
                    (when (and (some? ch-actual)
                               (not= ch-actual ch-expected)
                               text-before-cursor-matches?) "incorrect")
                    (when (and (some? ch-actual) 
                               (not text-before-cursor-matches?)) "after-incorrect")
                    (when (= character-index
                             (count text-actual)) "cursor")]))))))

 
(defn exercise-panel [{:keys [sheet-width sheet-height]}]
  (let [text-actual @(rf/subscribe [::exercise-subs/exercise-text-actual])
        text-expected @(rf/subscribe [::exercise-subs/exercise-text-expected])
        formatted-text @(rf/subscribe [::exercise-subs/exercise-text-formatted])
        progress @(rf/subscribe [::exercise-subs/exercise-progress])
        current-line-idx @(rf/subscribe [::exercise-subs/exercise-current-line])
        sheet-height @(rf/subscribe [::exercise-subs/exercise-sheet-height])
        whitespace-symbols {\space \u23b5 
                            \newline \u21b5}
        sheet-middle (quot (dec sheet-height) 2)]
    [:div#exercise.ui.raised.container.segment
     [:div.ui.top.attached.progress.success
      [:div.bar {:style {:width progress}}]]
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
               (whitespace-symbols ch ch)])])))
     [:div.ui.bottom.attached.progress.success
      [:div.bar {:style {:width progress}}]]]))


(defn exercise-view []
  [:div
   [:button.ui.button
    {:on-click #(rf/dispatch [::events/navigated-to-home])}  
    "Back"]
   [exercise-panel]])


(defn key-press-listener [e]
  (let [key->char {"Backspace" \backspace
                   "Enter" \newline}]
    (rf/dispatch [::exercise-events/character-typed (->
                                                     e
                                                     (.-key)
                                                     (#(get key->char % %)))])))


(defonce register-keypress-listener 
  (.addEventListener js/window "keydown" key-press-listener))

 
