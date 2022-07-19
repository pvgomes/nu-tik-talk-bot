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

(defn chama-lala [url]
  (println url))

(defn population-by-country-and-year [country-code year]
  (let [url (str "https://servicodados.ibge.gov.br/api/v1/paises/" country-code "/indicadores/77849?periodo=" year)
        lala (chama-lala url)
        raw-response (:body (http-client/get url))
        response (->> raw-response
                      json/read-json
                      first)
        country-series (:series response)
        population-years (-> country-series
                             first
                             (get-in [:serie])
                             first )]
    ((keyword year) population-years)))


(defn handle-population [params]
  (let [params-array (str/split params #" ")
        country-code (nth params-array 1)
        year (nth params-array 2)
        response (population-by-country-and-year country-code year)]
    (str "Total population in " year " are " response)))

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
      (t/send-text token id (handle-population (:text message)))))

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


  ;message mock
  ;{:message_id 71, :from {:id 746460687, :is_bot false, :first_name Paulo Victor, :last_name Gomes, :username pvgomes, :language_code en}, :chat {:id 746460687, :first_name Paulo Victor, :last_name Gomes, :username pvgomes, :type private}, :date 1658229947, :text /population BR 2022, :entities [{:offset 0, :length 11, :type bot_command}]}

  (def message-mock
    {:message_id 71
     :from {:id 746460687
            :is_bot false
            :first_name "Paulo"
            :last_name "Gomes"
            :username "pvgomes"
            :language_code "en"}
     :chat {:id 746460687
            :first_name "Paulo"
            :last_name "Gomes"
            :username "pvgomes"
            :type "private"}
     :date 1658229947
     :text "/population BR 2020"
     :entities [{
                 :offset 0
                 :length 11
                 :type "bot_command"}]})


  (println (handle-population (:text message-mock)))

  ; https://servicodados.ibge.gov.br/api/v1/paises/BR/indicadores/77849?indicadores=2022






  )