(ns walton.core
  (:use clojure.contrib.duck-streams
        clojure.contrib.str-utils
        clojure.contrib.seq-utils
        clj-html.core)
  (:gen-class))

(def *project-root* (System/getProperty "user.dir"))

(defhtml application [text body]
  [:html
   [:head
    [:title text]]
   [:body body]])

(defhtml header [text]
  [:h3 text])

(defhtml code-body [body]
  [:ul body])

(defhtml code-block [text]
  [:li [:pre text]])

;; Use this if you build the jar
(def logfiles
     (file-seq (java.io.File. (str *project-root* "/logs/"))))

;; Use this in your REPL
(def logfiles
     (file-seq (java.io.File. "/home/defn/git/walton/logs")))

(defn parse-log [logfile]
  (line-seq (reader logfile)))

(def parsed-logs
     (map parse-log (rest logfiles)))


;; Licenser is a mad genius.
(defn find-lines
"Search for the string [text] in [logs].

Usage: (find-lines \"zipmap\" parsed-logs)"
  [#^String text logs]
  (flatten
   (map
    (fn [log]
      (filter
       (fn [#^String line]
         (< 0 (.indexOf line text)))
       log))
    logs)))

(defn extract-code
"Extracts code blocks delimited by ( and ) which contain [text].

Usage: (extract-code \"zipmap\" parsed-logs"
  [text logs]
  (let [search-output (find-lines text logs)
        regex (re-pattern (str "\\(.*" text ".*\\)"))]
    (apply sorted-set (remove empty?
                        (map #(re-find regex %) search-output)))))

(defn walton-bare [text]
  (extract-code text parsed-logs))

(defn walton [text]
  (spit (java.io.File. (str *project-root* "/text.html"))
        (application text
          (html (header text)
                (code-body
                 (map code-block (extract-code text parsed-logs)))))))

(defn -main [& args]
  (let [search-term (str (first args))
        html? (str (second args))]
    (walton search-term)))