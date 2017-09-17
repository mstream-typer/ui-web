(ns typer-ui-web.config)


(def debug?
  ^boolean goog.DEBUG)


(def scheme
  (if debug?
    "http"
    "${SCHEME}"))


(def host
  (if debug?
    "localhost"
    "${HOST}"))


(def port
  (if debug?
    "8080"
    "${PORT}"))


(def api-base-url
  (str scheme "://" host ":" port "/api"))
