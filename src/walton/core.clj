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

  ;; Use this in your REPL
  (def logfiles
       (file-seq (java.io.File. "/home/defn/git/walton/logs")))

;; Use this if you build the jar
(def logfiles
   (rest (file-seq (java.io.File. (str *project-root* "/logs/")))))

(defn find-lines-in-file [#^String text file]
"Opens a file and rextracts the relevant lines from it.

Usage: (find-lines-in-file \"zipmap\" a-logfile)"
  (with-open [rdr (reader file)]
    (doall
      (filter (fn [#^String line]
         (< 0 (.indexOf line text)))
         (line-seq rdr)))))

;; Licenser is a mad genius.
(defn find-lines
"Search for the string [text] in [files].

Usage: (find-lines \"zipmap\" logfiles)"
  [#^String text files]
  (flatten
   (pmap
     (partial find-lines-in-file text)
     files)))

(defn extract-code
"Extracts code blocks delimited by ( and ) which contain [text].

Usage: (extract-code \"zipmap\" parsed-logs)"
  [text files]
  (let [search-output (find-lines text files)
        regex (re-pattern (str "\\(.*" text ".*\\)"))]
    (apply sorted-set (remove empty?
     (map #(re-find regex %) search-output)))))

(defn walton-bare [text]
  (extract-code text logfiles))

(defn walton [text]
  (spit (java.io.File. (str *project-root* "/text.html"))
        (application text
          (html (header text)
                (code-body
                 (map code-block (extract-code text logfiles)))))))

(defn -main [& args]
  (let [search-term (str (first args))
        html? (str (second args))]
    (walton search-term)))