(defproject walton "0.5"
  :description "Walton will traverse caves filled with snakes to find you example clojure code containing the function you desire."
  :dependencies [[org.clojure/clojure "1.1.0"]
		 [clj-sandbox/clj-sandbox "0.2.2-SNAPSHOT"]
                 [org.clojure/clojure-contrib "1.1.0"]
                 [clj-html "0.1.0"]]
  :dev-dependencies [[swank-clojure "1.1.0"]
                     [autodoc "0.7.0"]]
  :main walton.core
  :namespaces [walton.core])
