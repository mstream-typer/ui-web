(ns typer-ui-web.css
  (:require [garden.def :refer [defstyles]]
            [garden.units :refer [em px]]))


(defstyles screen
  [:span.line
   {:display "block"
    :min-height (em 1.25)}]

  ["#exercise"
   {:font-size (em 4)
    :letter-spacing (em 0.05)
    :line-height (em 1.25)
    :padding-bottom (em 0.25)
    :padding-top (em 0.25)}

   [".incorrect"
    {:background "#FF0000"
     :color "#880000 !important"}]

   [".after-incorrect"
    {:background "#FF8888"}]
   
   [".cursor"
    {:border-bottom-width (px 3)
     :border-bottom-style "solid"
     :border-bottom-color "#00FF00"}]

   [".character-untyped"
    {:color "#888888"}]

   [".character-typed"
    {:color "#000000"}]
   
   [".whitespace-untyped"
    {:color "#888888"}]

   [".whitespace-typed"
    {:color "#FFFFFF"}]])
