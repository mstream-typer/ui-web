(ns typer-ui-web.course.events
  (:require [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [typer-ui-web.db :as db]
            [typer-ui-web.course.db :as course-db]))


(s/def ::parameterless-event
  (s/tuple keyword?))











