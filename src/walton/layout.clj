(ns walton.layout
  (:use [hiccup core page-helpers form-helpers]))

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
    [:pre.brush:.clojure (str (:arglists (meta v)))]
    (when (:macro (meta v))
      [:b "Macro"])
    [:p (str (:doc (meta v)))]]))

(defn application [text body]
  (html
   [:html
    [:head
     (include-js "/javascript/syntaxhilighter/scripts/shCore.js"
                 "/javascript/syntaxhilighter/scripts/shBrushClojure.js")
     (include-css "/javascript/syntaxhilighter/styles/shCore.css"
                  "/javascript/syntaxhilighter/styles/shThemeDefault.css")
     [:script {:type "text/javascript"}
"//<![CDATA[
SyntaxHighlighter.defaults['gutter'] = false;
SyntaxHighlighter.defaults['toolbar'] = false;
SyntaxHighlighter.defaults['light'] = true;
SyntaxHighlighter.all();
//]]>"]
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
