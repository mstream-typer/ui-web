(ns typer-ui-web.views
  (:require [typer-ui-web.db :as db]
            [typer-ui-web.subs :as subs]
            [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]))


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


(defn main-menu []
  [:div.ui.large.menu
   [:div.right.menu
    [:a.item "Sign In"]]])


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


(defn main-panel []
  [:dev
   [main-menu]
   [exercise-panel]])

