(ns typer-ui-web.common.core
  (:require [re-frame.core :as rf]))


(def <sub (comp deref
                rf/subscribe))


(def evt> rf/dispatch)
