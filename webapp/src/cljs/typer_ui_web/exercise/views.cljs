(ns typer-ui-web.exercise.views
  (:require [typer-ui-web.common.core :refer [evt> <sub]]
            [typer-ui-web.exercise.db :as exercise-db]
            [typer-ui-web.exercise.events :as events]
            [typer-ui-web.exercise.subs :as subs]
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


(s/def ::hourglass-empty-when-exercise-not-started
  #(if (-> %
           (:args)
           (:exercise-started)
           (not))
     (= "hourglass empty icon"
        (:ret %))
     true))


(s/def ::hourglass-empty-when-exercise-finished
  #(if (-> %
           (:args)
           (:exercise-finished))
     (= "hourglass empty icon"
        (:ret %))
     true))


(s/def ::hourglass-start-when-in-first-third-of-time
  #(if (and (-> %
                (:args)
                (:exercise-started))
            (-> %
                (:args)
                (:exercise-finished)
                (not))
            (<= (-> %
                    (:args)
                    (:timer-initial)
                    (* 0.7))
                (-> %
                    (:args)
                    (:timer-current))
                (-> %
                    (:args)
                    (:timer-initial))))
     (= "hourglass start icon"
        (:ret %))
     true))


(s/def ::hourglass-half-when-in-second-third-of-time
    #(if (and (-> %
                (:args)
                (:exercise-started))
              (-> %
                  (:args)
                  (:exercise-finished)
                  (not))
              (< (-> %
                     (:args)
                     (:timer-initial)
                     (* 0.3))
                 (-> %
                     (:args)
                     (:timer-current))
                 (-> %
                     (:args)
                     (:timer-initial)
                     (* 0.7))))
       (= "hourglass half icon"
          (:ret %))
       true))


(s/def ::hourglass-end-when-in-last-third-of-time
  #(if (and (-> %
                (:args)
                (:exercise-started))
            (-> %
                (:args)
                (:exercise-finished)
                (not))
            (<= 0
                (-> %
                    (:args)
                    (:timer-current))
                (-> %
                    (:args)
                    (:timer-initial)
                    (* 0.3))))
     (= "hourglass end icon"
        (:ret %))
     true))


(s/fdef
 hourglass-class
 :args (s/and (s/cat :timer-current ::exercise-db/exercise-timer-current
                     :timer-initial ::exercise-db/exercise-timer-initial
                     :exercise-started boolean?
                     :exercise-finished boolean?)
              #(<= (:timer-current %)
                   (:timer-initial %))
              #(not (and (not (:exercise-started %))
                         (:exercise-finished %))))
 :ret string?
 :fn (s/and ::hourglass-empty-when-exercise-not-started
            ::hourglass-empty-when-exercise-finished
            ::hourglass-start-when-in-first-third-of-time
            ::hourglass-half-when-in-second-third-of-time
            ::hourglass-end-when-in-last-third-of-time))
(defn hourglass-class [current initial started finished]
  (let [state-class (if (or (not started)
                            finished)
                      "empty"
                      (cond
                        (<= 0
                            current
                            (* 0.3 initial)) "end"
                        (< (* 0.3 initial)
                           current
                           (* 0.7 initial)) "half"
                        (<= (* 0.7 initial)
                            current
                            initial) "start"))]
    (str "hourglass " state-class " icon")))


(defn exercise-panel [{:keys [sheet-width sheet-height]}]
  (let [text-actual (<sub [::subs/exercise-text-actual])
        text-expected (<sub [::subs/exercise-text-expected])
        formatted-text (<sub [::subs/exercise-text-formatted])
        progress (<sub [::subs/exercise-progress])
        current-line-idx (<sub [::subs/exercise-current-line])
        sheet-height (<sub [::subs/exercise-sheet-height])
        exercise-started? (<sub [::subs/exercise-started])
        exercise-finished? (<sub [::subs/exercise-finished])
        timer-current (<sub [::subs/exercise-timer-current])
        timer-current-formatted (<sub [::subs/exercise-timer-current-formatted])
        timer-initial (<sub [::subs/exercise-timer-initial])
        whitespace-symbols {\space \u23b5 
                            \newline \u21b5}
        sheet-middle (quot (dec sheet-height) 2)]
    [:div#exercise.ui.raised.container.segment
     [:div.ui.massive.blue.right.ribbon.label
      [:i {:class (hourglass-class timer-current
                                   timer-initial
                                   exercise-started?
                                   exercise-finished?)}] 
      timer-current-formatted]
     [:div.ui.top.attached.progress.success
      [:div.bar {:style {:width progress}}]]
     [:div.text
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
                (whitespace-symbols ch ch)])])))]
     [:div.ui.bottom.attached.progress.success
      [:div.bar {:style {:width progress}}]]]))


(defn exercise-menu [navigate-to-home-requested-event]
  [:div.ui.menu
   [:div.item
    [:div.ui.button
     {:on-click #(evt> navigate-to-home-requested-event)}
     "Back"]]
   [:div.item
    [:div.ui.button
     {:on-click #(evt> [::events/exercise-restarted])} 
     "Restart"]]])


(defn exercise-summary-modal [navigate-to-home-requested-event]
  (let [visible (<sub [::subs/summary-modal-open])
        modal-message (<sub [::subs/summary-modal-message])]
    [:div.ui.standard.modal.transition
     {:class (if true
               "visible active"
               "hidden")}
     [:div.header "Exercise finished"]
     [:div.content modal-message]
     [:div.actions
      [:div.ui.right.labeled.icon.button
       {:on-click #(evt> navigate-to-home-requested-event)}
       "Main menu"
       [:i.home.icon]]
      [:div.ui.right.labeled.icon.button
       {:on-click #(evt> [::events/exercise-restarted])} 
       "Repeat"
       [:i.repeat.icon]]
      [:div.ui.positive.right.labeled.icon.button
       {:on-click #(evt> [::events/exercise-restarted])} 
       "Next exercise"
       [:i.arrow.circle.right.icon]]]]))


(defn dimmer [navigate-to-home-requested-event]
  (let [active (<sub [::subs/modal-open])]
    [:div#dimmer.ui.dimmer.modals.page.transition
     {:class (if active
               "visible active"
               "hidden")}
     [exercise-summary-modal
      navigate-to-home-requested-event]]))


(defn exercise-view [navigate-to-home-requested-event]
  [:div
   [exercise-menu navigate-to-home-requested-event]
   [exercise-panel]
   [dimmer
    navigate-to-home-requested-event]])


(defn key-press-listener [e]
  (let [modifier-keys #{"Alt"
                        "Control"
                        "Meta"
                        "Shift"}
        key->char {"Backspace" \backspace
                   "Enter" \newline}
        key (.-key e)]
    (when (not (modifier-keys key))
      (evt> [::events/character-typed
             (-> e
                 (.-key)
                 (#(get key->char % %)))]))))


(defn dispatch-timer-ticked-event []
  (evt> [::events/timer-ticked]))


(defonce register-keypress-listener 
  (.addEventListener js/window "keydown" key-press-listener))


(defonce register-timer
  (js/setInterval dispatch-timer-ticked-event 1000))

 
