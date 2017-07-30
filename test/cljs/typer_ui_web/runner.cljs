(ns typer-ui-web.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [typer-ui-web.core-test]
              [typer-ui-web.views-test]
              [typer-ui-web.subs-test]
              [typer-ui-web.events-test]))


(doo-tests 'typer-ui-web.core-test
           'typer-ui-web.views-test
           'typer-ui-web.events-test
           'typer-ui-web.subs-test)
