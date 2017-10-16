(ns typer-ui-web.css
  (:require [garden.def :refer [defstyles]]
            [garden.units :refer [em px]]
            [cuerdas.core :as str]))


(def body-background
  (str/join
   ","
   ["radial-gradient(black 15%, transparent 16%) 0 0"
    "radial-gradient(black 15%, transparent 16%) 8px 8px"
    "radial-gradient(rgba(255,255,255,.1) 15%, transparent 20%) 0 1px"
    "radial-gradient(rgba(255,255,255,.1) 15%, transparent 20%) 8px 9px"]))

(def panel-shadow
  "0 1px 3px 0 #222222,0 0 0 1px #444444")


(defstyles screen
  [:body {:background body-background
          :background-color "#333333"
          :background-size "16px 16px"}]
  [".modal" {:max-height "calc(100% - 100px)"
             :transform "translate(0, -50%)"}]
  ["#main-menu" {:margin-bottom (em 4)}]
  ["#main-title" {:padding-top 0
                  :padding-bottom 0}
   [:h1 {:font-family "Bungee Shade"
         :font-size (em 4)}]]
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
    [".character-untyped" {:color "#666666"}]
    [".character-typed" {:color "#000000"}]
    [".whitespace-untyped" {:color "#AAAAAA"}]
    [".whitespace-typed" {:color "#FFFFFF"}]]]
  [".ui.cards > .card" {:box-shadow panel-shadow}])
