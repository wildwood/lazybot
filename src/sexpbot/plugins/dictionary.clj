(ns sexpbot.plugins.dictionary
  (:use [sexpbot respond info])
  (:require [com.twinql.clojure.http :as http]
	    [org.danlarkin.json :as json]
	    [irclj.irclj :as ircb])
  (:import java.net.URI))

(def wordnik-key (-> :wordnik-key get-key))

(defn extract-stuff [js]
  (let [text (:text js)]
    [(.replaceAll (if (seq text) text "") "\\<.*?\\>" "") (:partOfSpeech js)]))

(defn lookup-def [word]
  (-> (http/get 
       (URI. (str "http://api.wordnik.com/api/word.json/" word "/definitions"))
       :query {:count "1"}
       :headers {"api_key" wordnik-key}
       :as :string)
      :content json/decode-from-str first extract-stuff))

(defplugin 
  (:dict 
   "Takes a word and look's up it's definition via the Wordnik dictionary API." 
   ["dict"] 
   [{:keys [irc channel nick args]}]
   (ircb/send-message irc channel 
		      (str nick ": " 
			   (let [[text part] (lookup-def (first args))]
			     (if (seq text) (str part ": " text) "Word not found."))))))