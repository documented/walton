(ns walton.web
  (:use net.cgrand.enlive-html
        net.cgrand.moustache
        [ring.adapter.jetty :only [run-jetty]]
        walton.core))


(declare walton-web)
(def server (doto (Thread. #(run-jetty #'walton-web {:port 8080})) .start))


;; not working (yet)
(def walton-web
     (app
      ["examples" [text #".*"]]
      {:get [(walton-doc* text) ""]}
      [#".*"] {:get ["all routes!"]}))