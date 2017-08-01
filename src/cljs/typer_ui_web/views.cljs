(ns typer-ui-web.views
  (:require [typer-ui-web.db :as db]
            [typer-ui-web.events :as events]
            [typer-ui-web.subs :as subs]
            [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
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
         whitespace? (s/valid? ::db/whitespace (-> %
                                                   :args
                                                   :text-expected
                                                   (get char-idx)))
         result (% :ret)]
     (and (re-find (if whitespace?
                     #"\bwhitespace-\w+"
                     #"\bcharacter-\w+")
                   result)
          (not (re-find (if whitespace?
                          #"\bcharacter-\w+"
                          #"\bwhitespace-\w+")
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
                     #"\w+-typed\b"
                     #"\w+-untyped\b")
                   result)
          (not (re-find (if typed?
                          #"\w+-untyped\b"
                          #"\w+-typed\b")
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
        not) (re-find #"\bcursor\b"
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
         result (% :ret)]
     ((if (and (some? actual-char)
               (not= actual-char
                     expected-char))
        identity 
        not) (re-find #"\bincorrect\b"
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
        not) (re-find #"\bafter-incorrect\b"
                      result))))


(s/fdef
 character-class
 :args (s/and (s/cat :text-expected ::db/exercise-text
                     :text-actual string?
                     :character-index (s/and int? pos? #(<= % 10)))
              #(< (% :character-index) (-> % :text-expected count)))
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
           ch-expected (get text-expected character-index)]
       (if (>= character-index (count text-expected)) 
         "" 
         (str/join \space
                   [(str (if (s/valid? ::db/whitespace ch-expected)
                           "whitespace"
                           "character")
                         "-"
                         (if ch-actual
                           "typed"
                           "untyped"))
                    (when (and (some? ch-actual)
                               (not= ch-actual ch-expected)) "incorrect")
                    (when (and (some? ch-actual)
                               (not= (take character-index text-actual)
                                     (take character-index text-expected))) "after-incorrect")
                    (when (= character-index
                             (count text-actual)) "cursor")]))))))

 
(defn main-menu []
  [:div
   [:div.ui.large.menu
    [:div.right.menu
     [:a.item
      {:on-click #(rf/dispatch [::events/login-menu-button-pressed])}
      "Sign In"]]]])


(defn exercise-panel [{:keys [sheet-width sheet-height]}]
  (let [text-actual @(rf/subscribe [::subs/exercise-text-actual])
        text-expected @(rf/subscribe [::subs/exercise-text-expected])
        formatted-text @(rf/subscribe [::subs/exercise-text-formatted])
        progress @(rf/subscribe [::subs/exercise-progress])
        current-line-idx @(rf/subscribe [::subs/exercise-current-line])
        sheet-height @(rf/subscribe [::subs/exercise-sheet-height])
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


(defn login-menu []
  (let [active @(rf/subscribe [::subs/login-menu-visible])
        username @(rf/subscribe [::subs/login-menu-username])
        password @(rf/subscribe [::subs/login-menu-password])]
    [:div.ui.standard.modal.transition
     {:class (if active
               "visible active"
               "hidden")}
     [:div.header "Login"]
     [:div.content
      [:form.ui.form
       [:div.field
        [:label "Username"]
        [:input#username
         {:name "username"
          :type "text"
          :value username 
          :on-change #(rf/dispatch [::events/login-menu-username-changed
                                    (-> %
                                        .-target
                                        .-value)])}]]
       [:div.field
        [:label "Password"]
        [:input#password
         {:name "password"
          :type "password"
          :value password
          :on-change #(rf/dispatch [::events/login-menu-password-changed 
                                    (-> %
                                        .-target
                                        .-value)])}]]
     [:div.actions
      [:div.ui.black.deny.button
       {:on-click #(rf/dispatch [::events/cancel-login-menu-button-pressed])}
       "Cancel"]
      [:div.ui.positive.right.labeled.icon.button
       {:on-click #(rf/dispatch [::events/cancel-login-menu-button-pressed])}
       "Sign In"
       [:i.sign.in.icon]]]]]])) 


(defn dimmer []
  (let [active @(rf/subscribe [::subs/modal-opened])]
    [:div#dimmer.ui.dimmer.modals.page.transition
     {:class (if active
               "visible active"
               "hidden")}
     [login-menu]]))


(defn home-view []
  [:div
   [main-menu]
   [:button.ui.button
    {:on-click #(rf/dispatch [::events/navigated-to-exercise])}
    "Start"]])


(defn exercise-view []
  [:div
   [:button.ui.button
    {:on-click #(rf/dispatch [::events/navigated-to-home])} 
    "Back"]
   [exercise-panel]])


(defn view []
  (let [view @(rf/subscribe [::subs/view])]
    (case view
      :home [home-view]
      :exercise [exercise-view])))


(defn main-panel []
  [:div#main-panel
   [dimmer]
   [view]])


(defn key-press-listener [e]
  (let [key->char {"Backspace" \backspace
                   "Enter" \newline}]
   (rf/dispatch [::events/character-typed (-> e (.-key) (#(get key->char % %)))])))


(defonce register-keypress-listener 
  (.addEventListener js/window "keydown" key-press-listener))

