(ns walton.layout
  (:use [hiccup core page-helpers form-helpers]
        walton.core))

(defn application [text body]
     (html
      [:html
       [:head
        (include-js "../../resources/public/javascript/syntaxhilighter/scripts/shCore.js"
                    "../../resources/public/javascript/syntaxhilighter/scripts/shBrushClojure.js")
        (include-css
         "../../resources/public/javascript/syntaxhilighter/styles/shCore.css"
         "../../resources/public/javascript/syntaxhilighter/styles/shThemeDefault.css")
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
       [:pre.brush:.clojure code] [:pre.brush:.clojure ";; =&gt " result]]))
