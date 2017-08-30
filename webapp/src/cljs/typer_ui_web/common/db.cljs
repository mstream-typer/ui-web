(ns typer-ui-web.common.db
  (:require [clojure.spec.alpha :as s]))


(s/def ::id
  string?)


(s/def ::visible
  boolean?)


(s/def ::loader
  (s/keys :req [::visible]))
