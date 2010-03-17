(ns walton.core
  (:use clojure.contrib.duck-streams
        clojure.contrib.str-utils
        clojure.contrib.seq-utils))

(def logfiles
     (file-seq (java.io.File. "../../logs")))

(defn parse-log [logfile]
  (line-seq (reader logfile)))

(def parsed-logs
     (map parse-log (rest logfiles)))

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
    (remove empty?
            (map #(re-find regex %) search-output))))
