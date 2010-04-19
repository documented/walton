(ns walton.core
  (:use [clojure.contrib duck-streams seq-utils repl-utils str-utils]
        net.licenser.sandbox
        [hiccup core page-helpers form-helpers]
        net.cgrand.moustache
        [net.cgrand.enlive-html :exclude [flatten]]
        ring.util.response
        ring.middleware.file
        [ring.adapter.jetty :only [run-jetty]]
        [walton integration layout irc])
  (:require [org.danlarkin [json :as json]])
  (:gen-class))

(def *sandbox* (stringify-sandbox (new-sandbox-compiler :timeout 100)))
(def *sexps* (ref {}))
(def *project-root* (System/getProperty "user.dir"))
(def *walton-docs* (str *project-root* "/walton-docs/"))
(def logfiles (rest (file-seq (java.io.File. (str *project-root* "/logs/")))))

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

(defn truncate
  "A one off truncation function which takes a coll in the form of \"[:a, :b]\".  Provided a [t]runcation length (in characters), it will truncate :a or :b and supply a new \"[\":a...\", \":b...\"]\"."
  [coll t]
  (let [c coll
        ct (seq (first c))
        rt (seq (second c))]
    [(if (>= (count ct) t)
       (apply str (take t ct) "...")
       (apply str ct))
     (if (>= (count rt) t)
       (apply str (take t rt) "...")
       (apply str rt))]))

(defn walton
  "Returns a single random result where the the length of code, and the length of result are both limited to 497 characters each: [code, result]."
  [#^String s]
  (let [result (distinct (walton-doc s))
        random-result (nth result (rand-int (count result)))]
    (truncate random-result 497)))

(defn walton*
  "A more flexible version of walton which allows you to specify [s]:a string to search for, [t]:the number of characters to truncate at, [m?]:the number of docs you'd like as output, and [f]:if true, will filter bad results, if false, will show both good and bad results -- true by default."
  [#^String s t m?]
  (let [result (distinct (walton-doc s))]
    (if (>= m? 0)
      (map #(truncate % t) result)
      (if (>= m? 1)
        (take m? (map #(truncate % t) result))
        (let [random-result (nth result (rand-int (count result)))]
          (truncate random-result t))))))

(defn walton-html
  "Outputs the walton-doc results in HTML format for moustache, wrapped in a ring response handler."
  [#^String text]
  (let [results (walton* text 497 0)
        code-text (map first results)
        result-text (map second results)]
    (response
     (application
      text
      (map hide-show results)))))

(defn walton-html*
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


(declare walton-web)
(def server (doto (Thread. #(run-jetty #'walton-web {:port 8080})) .start))

(def walton-web
     (-> (app
          ["examples" text] {:get [(fn [req] (walton-html text))]}
          [#".*\.js"] {:get [(file-response "resources/public")]}
          [#".*\.css"] {:get [(file-response "resources/public")]})
         (wrap-file "resources/public")))

(defn -main
  [& args]
  (let [search-term (first args)]
    (do
      (println "Now generating" search-term ".html")
      (walton-html* search-term)
      (println "Now opening" search-term "in a browser.")
      (open-in-browser
       (str *walton-docs* search-term ".html")))))
