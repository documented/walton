(ns walton.core
  (:use clojure.contrib.duck-streams
        clojure.contrib.str-utils
        clojure.contrib.seq-utils
        clj-html.core
        net.licenser.sandbox
        walton.integration)
  (:gen-class))

(def *sandbox* (stringify-sandbox (new-sandbox-compiler :timeout 100)))
(def *sexps* (ref {}))
(def *project-root* (System/getProperty "user.dir"))
(def *walton-docs* (str *project-root* "/walton-docs/"))
(def logfiles (rest (file-seq (java.io.File. (str *project-root* "/logs/")))))

(defhtml application [text body]
  [:html
   [:head
    [:title text]]
   [:body
    [:h3 text]
    body]])

(defhtml code-list [body]
  [:ul body])

(defhtml code-block [[code result]]
  [:li [:pre code] [:pre ";; =&gt" result]])

(defn extract-expressions
  "Extracts sexps."
  [string]
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
"Collects all sexps in a single file."
  [file]
  (with-open [rdr (reader file)]
    (doall (flatten
            (map extract-expressions
                 (line-seq rdr))))))

(defn all-expressions
  "Collects all sexps in all files into a single sequence."
  [files]
  (flatten
   (map
    #(collect-expressions-in-file %) files)))

(defn find-lines-in-file
  "Opens a file and extracts the relevant lines from it."
  [#^String text file]
  (with-open [rdr (reader file)]
    (doall
     (filter (fn [#^String line]
               (< 0 (.indexOf line text)))
             (flatten (map extract-expressions (line-seq rdr)))))))

(defn find-lines
  "Search for the string [text] in [files], where files is the result of file-seq."
  [#^String text files]
  (flatten
   (map
    (partial find-lines-in-file text)
    files)))

(defn extract-working-code
  "Extract working code by running the code text in a try/catch."
  [#^String text files]
  (map (fn [code]
         (try
          (let [r ((*sandbox*  code) {})]
            [code (pr-str r)])
          (catch Exception e
            [code nil])))
       (find-lines text files)))

(defn add-sexp
  "Adds sexps to a ref after trying them in an explicit in a try/catch."
  [sexp]
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
  "Runs the expressions in a try/catch and categorizes them as :good or :bad."
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

(defn categorize-all
  "Categorizes all expressions."
  []
  (categorize-sexps (all-expressions logfiles)))

(defn background-init-walton
  "Categorizes all of the sexps in the background so you can search immediately."
  []
  (.start (Thread. (fn [] (dorun (map add-sexp (all-expressions logfiles)))))))

(defn init-walton
  "Categorizes all of the sexps up front."
  []
  (dosync
   (ref-set *sexps* (categorize-all)))
  true)
  
(defn walton-doc
  "Returns a sequence of all sexps with the tag :good which match the input string.  If no :good sexps are found, it returns the sexps which are tagged as :bad."
  [#^String s]
     (let [g (filter
              (fn [[#^String c r]] (< 0 (.indexOf c s)))
              (:good @*sexps*))]
       (if (not (empty? g))
	 g
	 (filter
          (fn [#^String c] (< 0 (.indexOf c s)))
          (:bad @*sexps*)))))

(defn walton
  "When passed a string, finds a random walton-doc which contains [s] and truncates either the input code, or the result, to get make very long results shorter.  For instance, (range 0 1000000) would have its result truncated."
  [#^String s]
  (let [result (walton-doc s)
        [code-text result-text] (nth result (rand-int (count result)))
        result-length (count result-text)
        code-length (count code-text)]
    [(if (>= (count code-text) 457)
       (apply str (take 457 (first result)) "...")
       code-text)
    (if (>= (count result-text) 457)
      (apply str (take 457 (second result)) "...")
      result-text)]))

(defn walton-html
  "Takes a string and then searches for all working expressions and outputs them as an html file into project-root/walton-docs/[text].html.  If there are no working results for the string, it will output the non-working examples which were found for [text]."
  [text]
  (let [results (extract-working-code text logfiles)
	good-results (filter second results)
        file-loc (str *project-root* "walton-docs/" text ".html")]
    (spit (java.io.File. file-loc)
          (application text
                       (code-list
                        (map code-block
                             (if (not (empty? good-results))
                               good-results
                               results)))))))

(defn -main
  [& args]
  (let [search-term (str (first args))]
    (do
      (println "Now generating" search-term ".html")
      (walton-html search-term)
      (println "Now opening" search-term "in a browser.")
      (open-in-browser
       (str *walton-docs* search-term ".html")))))
