(ns donetoday.core)
(use 'rotary.client)
(require '[clj-yaml.core :as yaml])
(use '[clojure.tools.cli :only [cli]])
(require '[clj-time.core :as time] )
;;(require '[clj-time.format])
(use 'clj-time.format)
(use 'clj-time.local)
;;(require '[clj-time.local])
(require '[clojure.pprint :as pprint]) 

(defn- user-prop
  "Returns the system property for user.<key>"
  [key]
  (System/getProperty (str "user." key)))


(def config  (yaml/parse-string
            (slurp
             (str  (user-prop "home") "/.aws.yaml"))))


(def aws-credential (select-keys config [:secret-key :access-key]))
(def dynamodb-formatter (formatter "yyyyMMdd"))
(def display-formatter (formatter "yyyy-MM-dd"))


(defn dyndb-today []  (Integer. (unparse dynamodb-formatter (local-now))))

(defn -main  [& args]
  "Record and view done things"
  (let [[options thingsdone banner] (cli args
                                         ["-v" "--view" "View things done" :flag true]
                                         ["-d" "--date" "The date you wish to view" :default (unparse display-formatter (time/minus (local-now) (time/months 1)))]
                                         ["-h" "--help" "Show help" :default false :flag true]
                                         )]
    (when (:help options)
      (println banner)
      (System/exit 0))
    (if (not thingsdone)
      (do (println "Things done required when not using --view")
          (System/exit 0)
          )
;      (println (first thingsdone))
      )
    (pprint/cl-format true "I did this today: ~%~{~A~%~}"  (sort (lazy-seq
             ((if (get-item aws-credential "donetoday" [(config :user) (dyndb-today)])
               (update-item  aws-credential "donetoday"  [(config :user) (dyndb-today)] {"done" [:add (set thingsdone) ]}  :return-values "ALL_NEW")
               (put-item aws-credential "donetoday" {"user" (config :user) "date" (dyndb-today) "done" thingsdone} :return-values "ALL_NEW"))
             "done"))))))
  




