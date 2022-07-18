(ns nu-tik-talk-bot.core
  (:require [clojure.core.async :refer [<!!]]
            [clojure.string :as str]
            [environ.core :refer [env]]
            [morse.handlers :as h]
            [morse.polling :as p]
            [morse.api :as t]
            [clj-http.client :as http-client]
            [clojure.data.json :as json])
  (:gen-class))

(def token (env :telegram-token))

(defn population-by-country-and-year [country-code year]
  (let [url (str "https://servicodados.ibge.gov.br/api/v1/paises/" country-code "/indicadores/77849")
        raw-response (:body (http-client/get url
                                             {:query-params {"periodo" year}}))
        response (->> raw-response
                      json/read-json
                      first)
        country-series (:series response)
        population-years (-> country-series
                             first
                             (get-in [:serie])
                             first )]
    ((keyword year) population-years)))

(h/defhandler handler

  (h/command-fn "start"
    (fn [{{id :id :as chat} :chat}]
      (println "Bot joined new chat: " chat)
      (t/send-text token id "Welcome to nu-tik-talk-bot!")))

  (h/command-fn "help"
    (fn [{{id :id :as chat} :chat}]
      (println "Help was requested in " chat)
      (t/send-text token id "Help is on the way")))

  (h/command-fn "population"
    (fn [{{id :id :as chat} :chat :as message}]
      (let [params (str/split message #" ")
            country-code (nth params 1)
            year (nth params 2)
            response (population-by-country-and-year country-code year)
            response-message (str "Total population in " year "are" response)]
        (t/send-text token id response-message))))

  (h/message-fn
    (fn [{{id :id} :chat :as message}]
      (println "Intercepted message: " message)
      (t/send-text token id "I don't do a whole lot ... yet."))))


(defn -main
  [& args]
  (when (str/blank? token)
    (println "Please provde token in TELEGRAM_TOKEN environment variable!")
    (System/exit 1))

  (println "Starting the nu-tik-talk-bot")
  (<!! (p/start token handler)))


(comment

  (str/split "/population BR 2020" #" ")

  (nth (str/split "/population BR 2020" #" ") 1)


  )