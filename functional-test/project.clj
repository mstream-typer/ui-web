(defproject typer-ui-web "0.1.0-SNAPSHOT"


  :dependencies
  [[org.clojure/clojure "1.9.0-alpha17"]
   [etaoin "0.1.6"]
   [http-kit "2.2.0"]
   [org.clojure/data.json "0.2.6"]]

  
  :plugins
  [[lein-ancient "0.6.10"]
   [lein-bikeshed "0.4.1"]
   [lein-cucumber "1.0.2"]
   [lein-kibit "0.1.6-beta2"]
   [lein-shell "0.5.0"]]

  
  :min-lein-version
  "2.5.3"


  :source-paths
  ["src/clj"]


  :clean-targets
  ^{:protect false}
  ["target"]


  :cucumber-feature-paths
  ["features/"]


  :cucumber-glue-paths
  ["src/clj"]


  :aliases
  {"docker-compose" ["do"
                     ["shell" "docker-compose" "down"]
                     ["shell" "docker-compose" "up" "-d"]]
   "ft" ["do"
         ["bikeshed"]
         ["kibit"]
         ["docker-compose"]
         ["cucumber" "--tags" "@current"]]})






