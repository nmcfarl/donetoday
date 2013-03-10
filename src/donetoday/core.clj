(ns donetoday.core
  (:gen-class)
  )
(use 'rotary.client)
(require '[clj-yaml.core :as yaml])
(use '[clojure.tools.cli :only [cli]])
(require '[clj-time.core :as time] )
(use 'clj-time.format)
(use 'clj-time.local)
(require '[clojure.pprint :as pprint])


(defn- user-prop
  "Returns the system property for user.<key>"
  [key]
  (System/getProperty (str "user." key)))


(def config  (yaml/parse-string
            (slurp
             (str  (user-prop "home") "/.donetoday"))))


(def aws-credential (select-keys config [:secret-key :access-key]))
(def dynamodb-formatter (formatter "yyyyMMdd"))
(def display-formatter (formatter "yyyy-MM-dd"))

(defn truncate-time [dt]
   (time/to-time-zone
   (apply time/date-time
          (map #(% dt)
               [time/year time/month time/day]))
   (time/default-time-zone)))



(defn dyndb-today []  (Integer. (unparse dynamodb-formatter (truncate-time (local-now)))))
(defn stringdate-to-int [date] (if date (Integer. (apply str (filter #(Character/isDigit %) date))) nil))

(defn view-day [view-date]  
  (pprint/cl-format true "I did this ~A: ~%~%~{* ~A~%~}~%" view-date
                    (sort
                     (lazy-seq
                      ((get-item aws-credential "donetoday" [(config :user) (stringdate-to-int view-date)])
                       "done")))))

(defn add-to-done-today [thingsdone date]
  (when-not (first thingsdone)
    (do (println "Things done required when not using --view")
        (System/exit 0)))
  (pprint/cl-format true "I did this today: ~%~%~{* ~A~%~}~%"
                    (sort
                     (lazy-seq
                      ((if (get-item aws-credential "donetoday" [(config :user) date])
                         (update-item  aws-credential "donetoday"  [(config :user) date] {"done" [:add (set thingsdone) ]}  :return-values "ALL_NEW")
                         (put-item aws-credential "donetoday" {"user" (config :user) "date" date "done" thingsdone} :return-values "ALL_NEW"))
                       "done")))))


(defn -main  [& args]
  "Record and view done things"
  (let [[options thingsdone banner] (cli args
                                         ["-v" "--view" "View things done" :flag true]
                                         ["-d" "--date" "The date you wish to view" :default (unparse display-formatter
                                                                                                      (truncate-time
                                                                                                       (time/minus (local-now) (time/months 1))))]
                                         ["-h" "--help" "Show help" :default false :flag true]
                                         )]
    (when (or (:help options) (not (or  (:view options) (first thingsdone))))
      (println banner)
      (System/exit 0))
    (if (:view options)
      (view-day (:date options)))
    (if (first thingsdone)
      (add-to-done-today  thingsdone (or
                                      (if (:view options)
                                        (stringdate-to-int (:date options)))
                                      (dyndb-today)  )))))
  


