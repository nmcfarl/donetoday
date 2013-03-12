(ns donetoday.core
  (:gen-class)
  )
(require '[clj-yaml.core :as yaml])
(require '[clojure.pprint :as pprint])
(require '[clj-time.core :as time] )
(use 'clj-time.format)
(use 'clj-time.local)
(use 'rotary.client)
(use '[clojure.tools.cli :only [cli]])


(def dynamodb-formatter (formatter "yyyyMMdd"))
(def display-formatter (formatter "yyyy-MM-dd"))

;;  Read the config file
(defn- user-prop
  "Returns the system property for user.<key>"
  [key]
  (System/getProperty (str "user." key)))

(def config  (yaml/parse-string
              (slurp
               (str  (user-prop "home") "/.donetoday"))))


(def aws-credential (select-keys config [:secret-key :access-key]))


;; date manipulation
(defn truncate-time [dt]
   (time/to-time-zone
   (apply time/date-time
          (map #(% dt)
               [time/year time/month time/day]))
   (time/default-time-zone)))

(defn- dyndb-today []  (Integer. (unparse dynamodb-formatter (truncate-time (local-now)))))
(defn- display-lastmonth [] (unparse display-formatter
                                     (truncate-time
                                      (time/minus (local-now) (time/months 1)))))
(defn- stringdate-to-integer [date] (if date (Integer. (apply str (filter #(Character/isDigit %) date))) nil))




;; actaul functionality

(defn- view-day [view-date]  
  (pprint/cl-format true "I did this ~A: ~%~%~{* ~A~%~}~%" view-date
                    (sort
                     (lazy-seq
                      ((get-item aws-credential "donetoday" [(config :user) (stringdate-to-integer view-date)])
                       "done")))))



(def tableinfo {:table "donetoday"   :type :hashandrange, :hash "user", :range "date" })

(defn key-from-tableinfo-data [tableinfo data]
  (if (= (:type tableinfo) :hash)
    [(data (:hash tableinfo))]
    (if (= (:type tableinfo) :hashandrange)
      [(data (:hash tableinfo)), (data (:range tableinfo))])))



(defn add-or-update-column [tableinfo data columnname]
  (let [[table key toadd] (list (:table tableinfo) (key-from-tableinfo-data tableinfo data) (if (string?  (data columnname))   #{ (data columnname)} (data columnname) ))]
    (println [table key toadd] )
    (if (get-item aws-credential table key)
       (update-item  aws-credential table key {columnname [:add toadd ]}  :return-values "ALL_NEW")
       (put-item aws-credential table data  :return-values "ALL_NEW"))
     ))





(defn- add-to-done-today [columnname thingsdone date]
  (when-not (first thingsdone)
    (do (println "Things done required when not using --view")
        (System/exit 0)))
  (pprint/cl-format true "I did this today: ~%~%~{* ~A~%~}~%"
                    (sort
                     (lazy-seq
                      ((add-or-update-column tableinfo  {"user" (config :user) "date" date columnname thingsdone} columnname) columnname)))))



(defn -main  [& args]
  "Record and view done things"
  (let [[options thingsdone banner] (cli args
                                         ["-v" "--view" "View things done" :flag true]
                                         ["-d" "--date" "The date you wish to view" :default (display-lastmonth)]
                                         ["-t" "--type" "Type of thing being added" :default "done" ]
                                         ["-h" "--help" "Show help" :default false :flag true]
                                         
                                         )]
    (when (or (:help options) (not (or  (:view options) (first thingsdone))))
      (println banner)
      (System/exit 0))
    (if (:view options)
      (view-day (:date options)))
    (if (first thingsdone)
      (add-to-done-today  (:type options) thingsdone (or
                                      (if (:view options)
                                        (stringdate-to-integer (:date options)))
                                      (dyndb-today)  )))))
  


