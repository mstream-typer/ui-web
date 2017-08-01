(ns typer-ui-web.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [re-frisk.core :refer [enable-re-frisk!]]
            [typer-ui-web.events]
            [typer-ui-web.subs]
            [typer-ui-web.views :as views]
            [typer-ui-web.config :as config]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (enable-re-frisk!)
    (println "dev mode")))


(defn mount-root []
  (rf/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))


(defn ^:export init []
  (rf/dispatch-sync [:db-initialized])
  (rf/dispatch-sync [:exercise-loaded])
  (dev-setup)
  (mount-root))


