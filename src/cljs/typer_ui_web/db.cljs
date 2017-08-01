(ns typer-ui-web.db
  (:require [clojure.spec.alpha :as s]))


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
  (s/and (s/coll-of ::exercise-character :gen-max 10)
         ::singly-whitespaced))


(s/def ::singly-whitespaced
  #(->> (partition-by (partial s/valid? ::whitespace) %)
        (filter (comp (partial s/valid? ::whitespace) first))
        (map count)
        (every? (partial > 2))))


(s/def ::actual
  (s/coll-of char?))


(s/def ::expected
  ::exercise-text)


(s/def ::text
  (s/keys :req [::actual ::expected]))


(s/def ::exercise
  (s/keys :req [::text]))


(s/def ::db
  (s/keys :req [::exercise]))

(s/def ::view #{:home :exercise})

(def default-db
  {::user {::username nil
           ::password nil}
   ::exercise {::text {::expected []
                       ::actual []}}
   ::ui {::view :home  
         ::exercise {::sheet {::height 5
                              ::width 20}}
         ::login-menu {::visible false
                       ::username ""
                       ::password ""}
         ::main-menu nil}})
