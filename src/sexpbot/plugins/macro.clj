(ns sexpbot.plugins.macro
  (:use [sexpbot respond info]
	clj-config.core
	stupiddb.core)
  (:require [irclj.irclj :as ircb]))

(def macros-file (str sexpdir "/macros.db"))
(def db (db-init macros-file 1800))

(defplugin
  (:add-hook :on-message
	     (fn [{:keys [irc nick message channel] :as irc-map}]
	       (let [macro (.trim message)
		     macro-body (db-get db macro)]
		 (when (not-empty macro-body)
		   (if (some identity (map #(.startsWith macro-body %) (:prepends (read-config info-file))))
		     (try-handle (assoc irc-map :message macro-body))
		     (ircb/send-message irc channel macro-body))))))
			  
  (:macro
   "Add a macro, a shorthand form of writing tedious commands -- Admin only"
   ["macro"]
   [{:keys [irc nick channel args] :as irc-map}]
   (let [macro-name (first args)
	 macro      (.trim (->> args (interpose " ") (rest) (apply str)))]
     (if (and (seq macro)
	      (seq macro-name))
       (if-admin nick irc-map
	 (do
	   (db-assoc db macro-name macro)
	   (ircb/send-message irc channel (str "Added macro: " macro-name))))
       (ircb/send-message irc channel (str nick ": please provide a macro name and body!")))))

   (:macroexpand
    "See what the named macro will do before executing it"
    ["macroexpand"]
    [{:keys [irc nick channel args] :as irc-map}]
    (let [macro-name (first args)
	  macro-body (db-get db macro-name)]
      (if (seq macro-body)
	(ircb/send-message irc channel (str nick ": " macro-name " => " macro-body))
	(ircb/send-message irc channel (str nick ": that macro doesn't exist!"))))))
      
    
       