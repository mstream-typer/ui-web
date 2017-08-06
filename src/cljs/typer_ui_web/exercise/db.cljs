(ns typer-ui-web.exercise.db
  (:require [clojure.spec.alpha :as s]))


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
  (s/and (s/coll-of ::exercise-character :kind vector?)
         ::singly-whitespaced))


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


(s/def ::timer (s/and int? (complement neg?)))


(s/def ::data
  (s/keys :req [::text ::started ::timer]))


(s/def ::sheet-size
  (s/and int? pos?))


(s/def ::height
  ::sheet-size)


(s/def ::width
  ::sheet-size)


(s/def ::sheet
  (s/keys :req[::height ::width]))
  

(s/def ::ui
  (s/keys :req [::sheet]))


(s/def ::exercise
  (s/keys :req [::data ::ui]))


(def default-db
  {::exercise {::data {::started false
                       ::timer 0
                       ::text {::expected dummy-text
                               ::actual []}}
               ::ui {::sheet {::height 5
                              ::width 20}}}})
