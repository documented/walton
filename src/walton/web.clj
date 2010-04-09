(ns walton.web
  (:use net.cgrand.enlive-html
        net.cgrand.moustache
        [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware.file]
        [ring.util.response]
        [walton core]))


(declare walton-web)
(def server (doto (Thread. #(run-jetty #'walton-web {:port 8080})) .start))

(def walton-web
     (-> (app
          ["examples" text] {:get [(fn [req] (walton-html text))]}
          [#".*"] {:get ["all routes!"]})
         (wrap-file "/home/defn/git/walton/resources/public")))

;; (GET "/application.js" (.getResourceAsStream 
;; (clojure.lang.RT/baseLoader) "my/app/application.js"))
