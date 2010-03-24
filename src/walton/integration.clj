(ns walton.integration
  (:use clojure.contrib.shell-out))

(defn- osascript [script]
  (let [mngr (javax.script.ScriptEngineManager.)
        engine (.getEngineByName mngr "AppleScript")]
    (.eval engine script)))

(defn open-in-browser-mac [file]
  (osascript "open " file))

(defn open-in-browser-linux [file]
  (let [which-firefox (apply str (drop-last (sh "which" "firefox")))]
    (sh which-firefox file)))

(defn open-in-browser [file]
  (let [this-os (System/getProperty "os.name")]
    (cond (= this-os "Linux")
          (open-in-browser-linux file)
          (= this-os "Mac OS X")
          (open-in-browser-mac file))))
