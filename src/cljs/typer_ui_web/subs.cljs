(ns typer-ui-web.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [typer-ui-web.db :as db]
            [re-frame.core :as rf]))

(rf/reg-sub
 :exercise-text-expected
 (fn [db]
   (-> db ::db/exercise ::db/text ::db/expected)))

(rf/reg-sub
 :exercise-text-actual
 (fn [db] 
   (-> db ::db/exercise ::db/text ::db/actual)))




