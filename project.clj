(defproject walton "0.5.4"
  :description "Walton will traverse caves filled with snakes to find you example clojure code containing the function you desire."
  :url "http://getclojure.org/"
  :dependencies [[org.clojure/clojure "1.2.0-master-SNAPSHOT"]
		 [clj-sandbox/clj-sandbox "0.2.10-SNAPSHOT"]
                 [org.clojure/clojure-contrib "1.2.0-master-SNAPSHOT"]
                 [clj-html "0.1.0"]
                 [net.cgrand/moustache "1.0.0-SNAPSHOT"]
                 [enlive "1.0.0-SNAPSHOT"]
                 [ring/ring-jetty-adapter "0.2.0"]
                 [ring "0.2.0"]]
  :dev-dependencies [[swank-clojure "1.2.0-SNAPSHOT"]
                     [autodoc "0.7.0"]]
  :main walton.core
  :namespaces [walton.core, walton.web, walton.integration])