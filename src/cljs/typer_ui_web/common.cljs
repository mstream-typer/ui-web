(ns typer-ui-web.common
  (:require [re-frame.core :as rf]))


(def <sub (comp deref
                rf/subscribe))


(def evt> rf/dispatch)
