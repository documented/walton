(ns walton.layout
  (:use [hiccup core page-helpers form-helpers]
        walton.core
        walton.web))

(defn application [text body]
     (html
      [:html
       [:head
        (include-js "/javascript/syntaxhilighter/scripts/shCore.js"
                    "/javascript/syntaxhilighter/scripts/shBrushClojure.js")
        (include-css "/javascript/syntaxhilighter/styles/shCore.css"
                     "/javascript/syntaxhilighter/styles/shThemeDefault.css")
        (javascript-tag "SyntaxHighlighter.all();")
        [:title text]]
       [:body
        [:h3 text]
        body]]))

(defn code-list [body]
     (html
      [:ul
       body]))

(defn code-block [[code result]]
     (html
      [:li
       [:pre.brush:.clojure code] [:pre.brush:.clojure result]]))
