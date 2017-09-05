(defproject typer-ui-web "0.1.0-SNAPSHOT"


  :dependencies
  [[org.clojure/clojure "1.9.0-alpha17"]
   [org.clojure/clojurescript "1.9.908"]
   [cljs-ajax "0.7.2"]
   [com.cemerick/url "0.1.1"]
   [day8.re-frame/http-fx "0.1.4"]
   [funcool/cuerdas "2.0.3"]
   [reagent "0.7.0"]
   [re-frame "0.10.1"]
   [re-frisk "0.5.0"]
   [garden "1.3.2"]
   [ns-tracker "0.3.1"]]


  :plugins
  [[lein-ancient "0.6.10"]
   [lein-bikeshed "0.4.1"]
   [lein-cljsbuild "1.1.6"]
   [lein-garden "0.3.0"]
   [macluck/lein-docker "1.3.0"]]


  :min-lein-version
  "2.5.3"


  :source-paths
  ["src/clj"]


  :clean-targets
  ^{:protect false}
  ["resources/public/css/compiled"
   "resources/public/js/compiled"
   "target"
   "test/js"]


  :profiles
  {:dev {:dependencies [[binaryage/devtools "0.9.4"]
                        [org.clojure/test.check "0.10.0-alpha2"]
                        [lein-doo "0.1.7"]]
         :plugins [[lein-figwheel "0.5.11"]
                   [lein-doo "0.1.7"]]}}
  

  :figwheel
  {:css-dirs ["resources/public/css"
              "resources/public/css/compiled"]}


  :garden
  {:builds [{:id "screen"
             :source-paths ["src/clj"]
             :stylesheet typer-ui-web.css/screen
             :compiler {:output-to "resources/public/css/compiled/screen.css"
                        :pretty-print? true}}]}
  

  :docker
  {:image-name "mstream/typer-ui-web"
   :dockerfile "Dockerfile"
   :build-dir  "."}


  :cljsbuild
  {:builds [{:id "dev"
             :source-paths ["src/cljs"]
             :figwheel {:on-jsload "typer-ui-web.core/mount-root"}
             :compiler {:main typer-ui-web.core
                        :output-to "resources/public/js/compiled/app.js"
                        :output-dir "resources/public/js/compiled/out"
                        :asset-path "js/compiled/out"
                        :source-map-timestamp true
                        :preloads [devtools.preload]
                        :external-config {:devtools/config {:features-to-install :all}}}}
            {:id "min"
             :source-paths ["src/cljs"]
             :compiler {:main typer-ui-web.core
                        :output-to "resources/public/js/compiled/app.js"
                        :optimizations :advanced
                        :closure-defines {goog.DEBUG false}
                        :pretty-print false}}
            {:id "test"
             :source-paths ["src/cljs"
                            "test/cljs"]
             :compiler {:main typer-ui-web.runner
                        :output-to "resources/public/js/compiled/test.js"
                        :output-dir "resources/public/js/compiled/test/out"
                        :optimizations :none}}]}

  :aliases
  {"build" ["do"
            ["deps"]
            ;["doo" "phantom" "test" "once"]
            ["cljsbuild" "once"]
            ["garden" "once"]
            ["docker" "build"]]})
