(ns typer-ui-web.home.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [typer-ui-web.home.db :as db]
            [typer-ui-web.common.db :as common-db]
            [clojure.spec.alpha :as s]
            [re-frame.core :as rf]))


(rf/reg-sub
 ::courses
 (comp ::db/courses
       ::db/data
       ::db/home))


(rf/reg-sub
 ::loading?
 (comp ::common-db/visible
       ::common-db/loader
       ::db/ui
       ::db/home))
