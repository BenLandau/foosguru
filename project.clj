(defproject foosguru "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure     "1.10.0"]
                 [org.clojure/java.jdbc   "0.7.5"]
                 [http-kit                "2.3.0-beta2"]
                 [ring/ring-json          "0.5.0"]
                 [compojure               "1.6.1"]
                 [org.xerial/sqlite-jdbc  "3.7.2"]]
  :main ^:skip-aot foosguru.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
