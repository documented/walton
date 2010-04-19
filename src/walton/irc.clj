(ns walton.irc
  (:use irclj.irclj
        walton.core))

(def *admin* "defn")

(defn check-user
  "Checks the nickname of the user.  This functions as a basic authorization check for commands like \"!join\"."
  [nick f]
  (if (= nick "defn")
    (f)
    nil))

(def bot-fnmap
     {:on-message
      (fn [{:keys [nick channel message irc]}]
        (let [[cmd & more] (.split message " ")]
          (condp = cmd
            "!join" (check-user nick #(join-chan irc (first more)))
            "!part" (check-user nick #(part-chan irc (first more)))
            "!identify" (send-message irc "nickserv" (str "identify youwish"))
            "!foo" (send-message irc "#defn-bot" "bar")
            "!walton" (if (empty? (first more))
                        (send-message irc "#defn-bot" (str "Usage: <user> !walton zipmap"))
                        (let [result (walton (first more))]
                          (send-message irc "#defn-bot" (first result))
                          (send-message irc "#defn-bot" (str "=> " (second result)))
                          (send-message irc nick (first result))
                          (send-message irc nick (str "=> " (second result)))))
            nil)))})

(def bot
     (connect
      (create-irc
       {:name "defn-bot",
        :server "irc.freenode.net",
        :username "defn-bot",
        :password "youwish"
        :port 6667,
        :realname "defn-bot"
        :fnmap bot-fnmap})
      :channels ["#defn-bot"]))

;; update bot-fnmap
(defn update-fnmap []
  (dosync
   (alter bot assoc :fnmap bot-fnmap)))

(dosync alter bot assoc :fnmap bot-fnmap)