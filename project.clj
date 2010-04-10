(defproject walton "0.5.4"
  :description "Walton will traverse caves filled with snakes to find you example clojure code containing the function you desire."
  :url "http://getclojure.org:8080/examples/concat"
  :dependencies [[org.clojure/clojure "1.2.0-master-SNAPSHOT"]
                 [org.clojure/clojure-contrib "1.2.0-master-SNAPSHOT"]
                 [clj-sandbox/clj-sandbox "0.2.13"]
                 [ring/ring-jetty-adapter "0.2.0"]
                 [ring "0.2.0"]
                 [hiccup "0.2.3"]
                 [net.cgrand/moustache "1.0.0-SNAPSHOT"]
                 [enlive "1.0.0-SNAPSHOT"]]
  :dev-dependencies [[swank-clojure "1.2.0-SNAPSHOT"]
                     [leiningen/lein-swank "1.2.0-SNAPSHOT"]
                     [autodoc "0.7.0"]]
  :main walton.core
  :resources-path "resources"
  :namespaces [walton.core])