(defproject typer-ui-web "0.1.0-SNAPSHOT"


  :dependencies
  [[org.clojure/clojure "1.9.0-alpha17"]
   [etaoin "0.1.6"]]

  
  :plugins
  [[lein-ancient "0.6.10"]
   [lein-bikeshed "0.4.1"]
   [lein-cucumber "1.0.2"]]

  
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
  ["src/clj"])


