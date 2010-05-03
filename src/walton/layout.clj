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

(defn handle-leftangle
  "If "
  [#^String s]
  (if (re-find #".*#<.*" s)
    (str "\"" s "\"")
    s))

;; (try
;;  (read-string (handle-leftangle code))
;;  (catch Exception _
;;    (identity code)))

(defn format-code*
  [& codes]
  (apply str (map
              (fn [code]
                (if (string? code)
                  (with-out-str
                    (pprint
                     (read-string code)))))
              codes)))

(defn format-code
  [& codes]
  (apply str (map
              (fn [code]
                (if (string? code)
                  (str code "\n")
                  (with-out-str
                    (pprint code))))
              codes)))

(defn one-liner?
  [s]
  (if s
    (< (count (remove empty? (s/split s #"\s*\n\s*"))) 2)
    true))

(defn code*
  "Show codes (literal strings or forms) in a pre/code block."
  [& codes]
  (let [code-string (apply format-code* codes)
        class-string "brush: clojure; toolbar: true;"]
    [:script {:type "syntaxhighlighter" :class class-string}
     (str "<![CDATA[" code-string "]]>")]))

(defn code
  "Show codes (literal strings or forms) in a pre/code block."
  [& codes]
  (let [code-string (apply format-code codes)
        class-string "brush: clojure; toolbar: true;"]
    [:script {:type "syntaxhighlighter" :class class-string}
     (str "<![CDATA[" code-string "]]>")]))

(defn hide-show [#^String s n]
  [:div.example
   [:div.left
    [:h1 (str n ". ")]]
   [:div.right
    [:div.code (code* (first s))]
    [:div.return (code (second s))]]])

(defn google-api []
  (html
   (include-js "http://www.google.com/jsapi?key=ABQIAAAAf81JAvR-zjjyFg4UPBcQ7hRUEEC_1sQajlg8OegeKNzx-mGf0RTW79MfTS7T6otdVJ5Q6A0_gqwzcg")
   (javascript-tag "google.load(jquery, 1.4.2);")))

(defn application [text body]
  (html
   [:html
    [:head
     (include-js "/javascript/jquery.js"
                 "/javascript/syntaxhilighter/scripts/shCore.js"
                 "/javascript/syntaxhilighter/scripts/shBrushClojure.js"
                 "/javascript/application.js")
     (include-css "/javascript/syntaxhilighter/styles/shCore.css"
                  "/javascript/syntaxhilighter/styles/shThemeDefault.css"
                  "/css/application.css")
     (javascript-tag "SyntaxHighlighter.defaults['gutter'] = false;
SyntaxHighlighter.defaults['toolbar'] = false;
SyntaxHighlighter.defaults['light'] = true;
SyntaxHighlighter.all();")
     [:title text]]
    [:body
     [:div#wrapper
      [:div#nav]
      [:div#content
       [:div.doc-header
        [:div.docstring
         [:h1 text]
         (let [sym (-> (str "clojure.core/" text) symbol find-var)]
           (if (meta sym)
             [:div.arglist (code* (str (:arglists (meta sym))))]))]
        (let [sym (-> (str "clojure.core/" text) symbol find-var)]
          (if (meta sym)
            (html [:p
                   (when (:macro (meta sym))
                     [:b "Macro" [:br]])
                   [:i (str (ns-name (:ns (meta sym))) "/"
                            (:name (meta sym)))] ": "
                   (str (:doc (meta sym)))])))]
       [:div.examples
        body]]]]]))


(defn code-list [body]
  (html
   [:ul
    body]))

(defn code-block [body]
  (html
   [:li
    body]))

(defn code-block [[code result]]
  (html
   [:div.example
    [:div.left]
    [:div.right
     [:div.code [:pre.brush:.clojure code]]
     [:div.return [:pre.brush:.clojure (str "; =&gt ") (str result)]]]]))
