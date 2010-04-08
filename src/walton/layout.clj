(ns walton.layout
  (:use clj-html.core
        walton.core))

(defhtml application [text body]
  [:html
   [:head
    [:title text]]
   [:body
    [:h3 text]
    body]])

(defhtml code-list [body]
  [:ul body])

(defhtml code-block [[code result]]
  [:li [:pre code] [:pre ";; =&gt " result]])
