(defproject donetoday "0.1.0-SNAPSHOT"
  :description "Simple to-done list UI, with support for adding things done today and viewing past days"
  :main donetoday.core
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"],
                 [org.clojars.crowdflower/rotary "0.3.8"],
                 [clj-yaml "0.4.0"],
                 [clj-time "0.4.4"],
                 [org.clojure/tools.cli "0.2.2"]
                 ]
  :plugins [[lein-ring "0.4.5"]]
  :profiles {:dev {:dependencies [[midje "1.5.0"]]}}
  )
