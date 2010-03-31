(ns walton.core
  (:use clojure.contrib.duck-streams
        clojure.contrib.str-utils
        clojure.contrib.seq-utils
        clj-html.core
        net.licenser.sandbox
        walton.integration)
  (:gen-class))

;; initial configuration
(def *sandbox* (stringify-sandbox (new-sandbox-compiler :timeout 100)))
(def *sexps* (ref {}))

(def *project-root* (System/getProperty "user.dir"))
(def *walton-docs* (str *project-root* "/walton-docs/"))
(def logfiles (rest (file-seq (java.io.File. (str *project-root* "/logs/")))))

;; layout
(defhtml application [text body]
  [:html
   [:head
    [:title text]]
   [:body
    [:h3 text]
    body]])

(defhtml code-body [body]
  [:ul body])

(defhtml code-block [[code result]]
  [:li [:pre code] [:pre ";; =&gt" result]])

(defhtml pre-tag [line]
  [:pre line])

;; sexp extraction
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

(defn collect-expressions-in-file
"Collects all sexps in a file."
  [file]
  (with-open [rdr (reader file)]
    (doall (flatten (map extract-expressions (line-seq rdr))))))

(defn all-expressions
  "Collects all sexps in all files."
  [files]
  (flatten
   (pmap
    #(collect-expressions-in-file %) files)))

(defn find-lines-in-file
  "Opens a file and rextracts the relevant lines from it.
Usage: (find-lines-in-file \"zipmap\" a-logfile)"
  [#^String text file]
  (with-open [rdr (reader file)]
    (doall
     (filter (fn [#^String line]
               (< 0 (.indexOf line text)))
             (flatten (map extract-expressions (line-seq rdr)))))))

(defn find-lines
  "Search for the string [text] in [files].
Usage: (find-lines \"zipmap\" logfiles)"
  [#^String text files]
  (flatten
   (pmap
    (partial find-lines-in-file text)
    files)))

;; (defn extract-code
;;   "Extracts code blocks delimited by ( and ) which contain [text].

;; Usage: (extract-code \"zipmap\" parsed-logs)"
;;   [text files]
;;   (let [search-output (find-lines text files)
;;         regex (re-pattern (str "\\(.*" text ".*\\)"))]
;;     (apply sorted-set (flatten (remove empty?
;;      (map extract-expressions search-output))))))

(defn extract-working-code [#^String text files]
  (map (fn [code]
         (try
          (let [r ((*sandbox*  code) {})]
            [code (pr-str r)])
          (catch Exception e
            [code nil])))
       (find-lines text files)))


(defn add-sexp [sexp]
  (binding [*out* nil
	    *err* nil]
    
    (try
     (let [r ((*sandbox* sexp) {'*out* nil '*err* nil})]
       (dosync 
	(alter *sexps* update-in [:good]
	       conj [sexp (pr-str r)])))
     (catch java.lang.Throwable t
       (dosync (alter *sexps* update-in [:bad]
		      conj sexp))))))

(defn categorize-sexps
  ([sexps cats]
     (reduce (fn [result, code]
	       (try
		(let [r ((*sandbox* code) {})]
		  (update-in result [:good]
			     conj [code (pr-str r)]))
		(catch java.lang.Throwable t
		  (update-in result [:bad]
			     conj code))))
	     cats
	     sexps))
  ([sexps] (categorize-sexps sexps {})))

(defn categorize-all []
  (categorize-sexps
          (all-expressions logfiles)))



(defn background-init-walton []
  (.start (Thread. (fn [] (dorun (map add-sexp (all-expressions logfiles)))))))

(defn init-walton []
  (dosync
   (ref-set *sexps* (categorize-all)))
  true)
  
  
(defn w-doc [#^String s]
     (let [g (filter (fn [[#^String c r]] (< 0 (.indexOf c s))) (:good @*sexps*))]
       (if (not (empty? g))
	 g
	 (filter (fn [#^String c] (< 0 (.indexOf c s))) (:bad @*sexps*)))))

;; (defn walton-bare [text]
;;   (extract-code text logfiles))

;; (defn walton-working [text]
;;   (extract-working-code text logfiles))

(defn walton [text]
  (let [results (extract-working-code text logfiles)
	good-results (filter second results)
        file-loc (str *project-root* "walton-docs/" text ".html")]
    (spit (java.io.File. file-loc)
          (application text
                       (code-body
                        (map code-block
                             (if (not (empty? good-results)) good-results results)))))))



;; MAIN
(defn -main [& args]
  (let [search-term (str (first args))]
    (do
      (println "Now generating" search-term ".html")
      (walton search-term)
      (println "Now opening" search-term "in a browser.")
      (open-in-browser
       (str *walton-docs* search-term ".html")))))
