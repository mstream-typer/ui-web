(ns typer-ui-web.common.events
  (:require [clojure.spec.alpha :as s]
            [typer-ui-web.db :as core-db]))


(s/def ::event-handler-db
  (s/keys :req-un [::core-db/db]))


(s/def ::coeffects
  ::event-handler-db)


(s/def ::effects
  ::event-handler-db)


(s/def ::parameterless-event
  (s/tuple keyword?))


(s/def ::failure-event
  (s/tuple keyword?
           string?))


