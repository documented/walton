(ns walton.core
  (:use clojure.contrib.duck-streams
        clojure.contrib.str-utils
        clojure.contrib.seq-utils
        clj-html.core
        net.licenser.sandbox
        walton.integration)
  (:gen-class))

(def *sandbox* (stringify-sandbox (new-sandbox :timeout 100)))

(def *project-root* (System/getProperty "user.dir"))

(def *walton-docs* (str *project-root* "walton-docs/"))


(defn extract-expressions [string]
  (second
   (reduce (fn [[exp exps state cnt] c]
	     (cond
	      (= state :escape)
	      [(.append exp c) exps :string cnt]
	      (= state :string) (cond
				 (= c \")
				 [(.append exp c) exps :code cnt]
				 (= c \\)
				 [(.append exp c) exps :escape cnt]
				 (= c \\)
				 [(.append exp c) exps :escape cnt]
				 :else
				 [(.append exp c) exps :string cnt])
	      (and (= cnt 1) (= c \)))
  	      [(java.lang.StringBuilder.) (cons (str (.append exp c)) exps) :text 0]
	      (= c \()
	      [(.append exp c) exps :code (inc cnt)]
	      (and (> cnt 1) (= c \)))
	      [(.append exp c) exps :code (dec cnt)]
	      (and (> cnt 0) (= c \"))
	      [(.append exp c) exps :string cnt]
	      (> cnt 0)
	      [(.append exp c) exps :code cnt]
	      :else [exp exps state cnt]))
	   [(java.lang.StringBuilder.) '() :text 0]
	   string)))

(defhtml application [text body]
  [:html
   [:head
    [:title text]]
   [:body body]])

(defhtml header [text]
  [:h3 text])

(defhtml code-body [body]
  [:ul body])

(defhtml code-block [[code result]]
  [:li [:pre code] ";; =&gt" [:pre result]])


;; Use this in your REPL
;; (def logfiles
;;      (file-seq (java.io.File. "/home/defn/git/walton/logs")))
;; M-x slime-set-default-directory "/home/project/root" will do the trick also
;; M-x cd "/home/project/root" just to be sure...

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
         (flatten (map extract-expressions (line-seq rdr)))))))

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
    (apply sorted-set (flatten (remove empty?
     (map extract-expressions search-output))))))

(defn extract-working-code [#^String text files]
    (map (fn [code]
        (try
          (let [r (*sandbox*  code)]
          [code (pr-str r)])
          (catch Exception e
            [code nil])))
        (find-lines text files)))

(defn walton-bare [text]
  (extract-code text logfiles))

(defn walton-working [text]
  (extract-working-code text logfiles))

(defn walton [text]
  (let [results (extract-working-code text logfiles)]
    (spit (java.io.File. (str *walton-docs* text ".html"))
          (application text
            (html (header text)
              (code-body
                (map code-block (filter second results))))))))

(defn -main [& args]
  (let [search-term (str (first args))]
    (do
      (println "Now generating" search-term ".html")
      (walton search-term)
      (println "Now opening" search-term "in a browser.")
      (open-in-browser
       (str *walton-docs* search-term ".html")))))
