(ns walton.layout
  (:use [hiccup core page-helpers form-helpers]
        [clojure.contrib pprint with-ns])
  (:require [clojure.contrib.str-utils2 :as s]))

(defn unveil-namespace
  "Gets all of the vals in the map produced by (ns-publics 'ns)"
  [ns]
  (vals (ns-publics ns)))

(defn format-docstring
  "Formats the docstring for the web."
  [v]
  (html
   [:blockquote [:i (str (ns-name (:ns (meta v))) "/" (:name (meta v)))]
    [:br]
    [:pre.brush:.clojure {:style "background-color:#eee;"} (str (:arglists (meta v)))]
    (when (:macro (meta v))
      [:b "Macro"])
    [:p (str (:doc (meta v)))]]))

(defn format-code
  [& codes]
  (apply str (map
              (fn [code]
                (if (string? code)
                  (str code "\n")
                  (with-out-str (pprint code))))
              codes)))

(defn one-liner?
  [s]
  (if s
    (< (count (remove empty? (s/split s #"\s*\n\s*"))) 2)
    true))

(defn code*
  "Show codes (literal strings or forms) in a pre/code block."
  [& codes]
  (let [code-string (apply format-code codes)
        class-string "brush: clojure; toolbar: true;"
        class-string (if (one-liner? code-string) (str class-string  " light: true;") class-string)]
    [:script {:type "syntaxhighlighter" :class class-string}
     (str "<![CDATA[" code-string "]]>")]))

(defn hide-show [#^String s]
  [:div {:class "toggle"}
   [:div [:a {:href "javascript:void(null)"} (code* (first s))]]
   [:div {:style "display:none;"} [:a {:href "javascript:void(null)"} (code* (first s) (second s))]]])

(defn google-api []
  (html
   (include-js "http://www.google.com/jsapi?key=ABQIAAAAf81JAvR-zjjyFg4UPBcQ7hRUEEC_1sQajlg8OegeKNzx-mGf0RTW79MfTS7T6otdVJ5Q6A0_gqwzcg")
   (javascript-tag "google.load(\"jquery\", \"1.4.2\");
google.load(\"jqueryui\", \"1.8.0\");")))

(defn application [text body]
  (html
   [:html
    [:head
     (include-js "/javascript/syntaxhilighter/scripts/shCore.js"
                 "/javascript/syntaxhilighter/scripts/shBrushClojure.js"
                 "/javascript/jquery-1.4.2.js"
                 "/javascript/application.js")
     (include-css "/javascript/syntaxhilighter/styles/shCore.css"
                  "/javascript/syntaxhilighter/styles/shThemeDefault.css"
                  "/css/application.css")
     (javascript-tag "SyntaxHighlighter.defaults['gutter'] = false;
SyntaxHighlighter.defaults['toolbar'] = true;
SyntaxHighlighter.defaults['light'] = false;
SyntaxHighlighter.all();")
     [:title text]]
    [:body
     [:h1 text]
     (let [sym (-> (str "clojure.core/" text) symbol find-var)]
       (if (meta sym)
         (format-docstring sym)))
     body]]))

(defn code-list [body]
  (html
   [:ul
    body]))

(defn code-block [[code result]]
  (html
   [:li
    [:pre.brush:.clojure code]
    [:pre.brush:.clojure (str "; =&gt ") (str result)]]))
