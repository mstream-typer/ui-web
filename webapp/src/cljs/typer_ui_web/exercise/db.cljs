(ns typer-ui-web.exercise.db
  (:require [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [typer-ui-web.common.db :as common-db]))


(def dummy-text
  (vec (concat "aaaaaaaaaa aaaaaaaaaaa aaaaaaaaaaa aaaaaaaaaaa"
               [\newline]
               "bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb bbb"
               [\newline]
               "cccccccccc ccccccccccc ccccccccccc ccccccccccc"
               [\newline]
               "aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa aa")))
 

(def characters
  #{\a \b \c \d \e \f \g \h \i \j \k \l \m
    \n \o \p \q \r \s \t \u \v \w \x \y \z
    \A \B \C \D \E \F \G \H \I \J \K \L \M
    \N \O \P \Q \R \S \T \U \V \W \X \Y \Z
    \! \@ \# \$ \% \^ \& \* \( \) \- \_ \=
    \+ \[ \{ \] \} \; \: \" \' \\ \| \, \<
    \. \> \/ \? \` \~})


(def whitespaces
  #{\newline \space})


(s/def ::character
  characters)


(s/def ::whitespace
  whitespaces)


(s/def ::exercise-character
  (into characters whitespaces))


(s/def ::exercise-text
  (let [spec (s/and (s/coll-of ::exercise-character :kind vector?)
         ::singly-whitespaced)]
    (s/with-gen spec #(gen/resize 10 (s/gen spec)))))  


(s/def ::actual-text
  (s/coll-of char? :kind vector?))


(s/def ::singly-whitespaced
  #(->> (partition-by (partial s/valid? ::whitespace) %)
        (filter (comp (partial s/valid? ::whitespace) first))
        (map count)
        (every? (partial > 2))))


(s/def ::actual
  ::actual-text)


(s/def ::expected
  ::exercise-text)


(s/def ::text
  (s/keys :req [::actual ::expected]))


(s/def ::started boolean?)


(s/def ::finished boolean?)


(s/def ::exercise-timer-current
  (s/and int? (complement neg?)))


(s/def ::exercise-timer-initial
  (s/and int? pos?))


(s/def ::current
  ::exercise-timer-current)


(s/def ::initial
  ::exercise-timer-initial)


(s/def ::timer
  (s/and (s/keys :req [::current ::initial])
         #(<= (:current %)
              (:initial %))))


(s/def ::data
  (s/and (s/keys :req [::text
                       ::started
                       ::finished
                       ::timer])
         #(not (and (not (::started %))
                         (::finished %)))))


(s/def ::sheet-size
  (s/and int? pos?))


(s/def ::height
  ::sheet-size)


(s/def ::width
  ::sheet-size)


(s/def ::sheet
  (s/keys :req [::height ::width]))


(s/def ::visible
  boolean?)


(s/def ::summary-modal
  (s/keys :req [::common-db/visible]))
  

(s/def ::ui
  (s/keys :req [::sheet]))


(s/def ::exercise
  (s/keys :req [::data ::ui]))


(def default-db
  {::exercise {::data {::started false
                       ::finished false
                       ::timer {::current 100
                                ::initial 100}
                       ::text {::expected dummy-text
                               ::actual []}}
               ::ui {::sheet {::height 5
                              ::width 25}
                     ::summary-modal {::common-db/visible false}}}})
