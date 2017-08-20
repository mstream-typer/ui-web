(ns typer-ui-web.css
  (:require [garden.def :refer [defstyles]]
            [garden.units :refer [em px]]))


(defstyles screen
  [".modal" {:max-height "calc(100% - 100px)"
             :transform "translate(0, -50%)"}]
  ["#exercise" {:letter-spacing (em 0.05)
                :line-height (em 1.25)
                :padding-bottom (em 0.25)
                :padding-top (em 0.25)}
   [:span.line {:display "block"
                :min-height (em 1.25)}]
   [".text" {:font-size (em 4)}
    [".incorrect" {:background "#FF0000"
                   :color "#880000 !important"}]
    [".after-incorrect" {:background "#FF8888"}]
    [".cursor" {:border-bottom-width (px 3)
                :border-bottom-style "solid"
                :border-bottom-color "#00FF00"}]
    [".character-untyped" {:color "#888888"}]
    [".character-typed" {:color "#000000"}]
    [".whitespace-untyped" {:color "#888888"}]
    [".whitespace-typed" {:color "#FFFFFF"}]]])