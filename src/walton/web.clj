(ns walton.web
  (:use net.cgrand.enlive-html
        net.cgrand.moustache
        [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware.file]
        [ring.util.response]
        [walton core layout]))

(declare walton-web)
(def server (doto (Thread. #(run-jetty #'walton-web {:port 8080})) .start))

(def walton-web
     (-> (app
          ["examples" text] {:get [(fn [req] (walton-html text))]}
          [#".*\.js"] {:get [(file-response "resources/public")]}
          [#".*\.css"] {:get [(file-response "resources/public")]})
         (wrap-file "resources/public")))
