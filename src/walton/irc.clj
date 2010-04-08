 (ns walton.irc
   (:import (java.net Socket)
            (java.io PrintWriter InputStreamReader BufferedReader)))

 (def freenode {:name "irc.freenode.net" :port 6667})
 (def user {:name "defn-bot" :nick "defn-bot"})

 (declare conn-handler)

 (defn connect [server]
   (let [socket (Socket. (:name server) (:port server))
         in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
         out (PrintWriter. (.getOutputStream socket))
         conn (ref {:in in :out out})]
     (doto (Thread. #(conn-handler conn)) (.start))
     conn))

 (defn write [conn msg]
   (doto (:out @conn)
     (.println (str msg "\r"))
     (.flush)))

(defn reader [conn msg]
  (doto (:in @conn)
    ;; Here is some shit))
    ))

 (defn conn-handler [conn]
   (while 
    (nil? (:exit @conn))
    (let [msg (.readLine (:in @conn))]
      (println msg)
      (cond 
       (re-find #"^ERROR :Closing Link:" msg) 
       (dosync (alter conn merge {:exit true}))
       (re-find #"^PING" msg)
       (write conn (str "PONG "  (re-find #":.*" msg)))))))

 (defn login [conn user]
   (write conn (str "NICK " (:nick user)))
   (write conn (str "USER " (:nick user) " 0 * :" (:name user))))

 ;;(def irc (connect freenode))
 ;;(login irc user)
 ;;(write irc "JOIN #clojure")
 ;;(write irc "QUIT")