(ns typer-ui-web.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [re-frisk.core :refer [enable-re-frisk!]]
            [typer-ui-web.events]
            [typer-ui-web.subs]
            [typer-ui-web.views :as views]
            [typer-ui-web.config :as config]))


(defn key-press-listener [e]
  (let [char-codes {8 \backspace
                    10 \newline
                    13 \newline}]
    (.preventDefault e) 
    (rf/dispatch-sync [:character-typed (get char-codes
                                             (.-keyCode e)
                                             (-> e .-charCode char))])))


(defn register-keypress-listener [] 
  (.removeEventListener js/window "keypress" key-press-listener)
  (.addEventListener js/window "keypress" key-press-listener))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (enable-re-frisk!)
    (println "dev mode")))


(defn mount-root []
  (rf/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app"))
  (register-keypress-listener))


(defn ^:export init []
  (rf/dispatch-sync [:db-initialized])
  (rf/dispatch-sync [:exercise-loaded])
  (dev-setup)
  (mount-root))



