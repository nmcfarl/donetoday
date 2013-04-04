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

; constants
(def dynamodb-formatter (formatter "yyyyMMdd"))
(def display-formatter (formatter "yyyy-MM-dd"))
(def tableinfo {:table "donetoday"   :type :hashandrange, :hash "user", :range "date" })
(defn dynamodb2display [date]  (unparse display-formatter (parse  dynamodb-formatter (str date))))


;;  Read the config file
(defn- user-prop
  "Returns the system property for user. <key>"
  [key]
  (System/getProperty (str "user." key)))

(def config
  (yaml/parse-string
              (slurp
               (str  (user-prop "home") "/.donetoday"))))


(def aws-credential (select-keys config [:secret-key :access-key]))


;; date manipulation
(defn truncate-time 
  "Trunctate a time to the date"
  [dt]
   (time/to-time-zone
   (apply time/date-time
          (map #(% dt)
               [time/year time/month time/day]))
   (time/default-time-zone)))

(defn- today []  (unparse dynamodb-formatter (truncate-time (local-now))))
(defn- lastmonth [] (unparse display-formatter
                                     (truncate-time
                                      (time/minus (local-now) (time/months 1)))))
(defn- yesterday [] (unparse display-formatter
                                     (truncate-time
                                      (time/minus (local-now) (time/days 1)))))

(defn- stringdate-to-integer [date] (if date (Integer. (apply str (filter #(Character/isDigit %) date))) nil))
(defn- date->integer [date] (if date (Integer. (apply str (filter #(Character/isDigit %)  (unparse dynamodb-formatter date)))) nil))


;;DynamoDB stuff

(defn key-from-tableinfo-data
  "Extract information about the key for this table, from our table info structure"
  [tableinfo data]
  (if (= (:type tableinfo) :hash)
    [(data (:hash tableinfo))]
    (if (= (:type tableinfo) :hashandrange)
      [(data (:hash tableinfo)), (data (:range tableinfo))])))



(defn add-or-update-column 
  "Add an a item to column in DynamoDB"
  [tableinfo data columnname]
  (let [[table key toadd] (list (:table tableinfo) (key-from-tableinfo-data tableinfo data) (if (string?  (data columnname))   #{ (data columnname)} (data columnname) ))]
    (if (get-item aws-credential table key)
       (update-item  aws-credential table key {columnname [:add toadd ]}  :return-values "ALL_NEW")
       (put-item aws-credential table data  :return-values "ALL_NEW"))
     ))



(defn- dt-get-column
  "internal colum"
  [columnname view-date]
  (sort
   (lazy-seq
    ((get-item aws-credential  (:table tableinfo) [(config :user) view-date])
     columnname))))

(defn- dt-add-or-update-column
  "internal update"
  [columnname thingsdone date]
  (sort
   (lazy-seq
    ((add-or-update-column tableinfo  {"user" (config :user) "date" date columnname thingsdone} columnname) columnname))))


;; actaul functionality

(defn- gen-day
  "Just show a days info"
  [columnname view-date]
      (pprint/cl-format nil "--------------------------------------~%I did this ~A: ~%~%~{* ~A~%~}~%" (dynamodb2display view-date) (dt-get-column columnname view-date)))


(defn- view-day
  "Just show a days info"
  [columnname view-date]
  (println (gen-day columnname view-date)))

(defn- get-date-from-options
  "figure out the date from the options"
  [options default]
  (stringdate-to-integer
   (if (:yesterday options)
     (yesterday)
     (if (:date options)
       (:date options)
       default))))
                                     

(defn- view-month
  "runs view day for a month"
  [columnname view-date]
  (pprint/cl-format true "~{~A~}~%"
      (map #(gen-day columnname
                              (date->integer
                               (time/minus 
                                (parse dynamodb-formatter (str view-date))
                                (time/days %))))
           (reverse (range 1 31)))))     

                                     
                                     
(defn- add-to-done-today
  "Add an item to today's list."
  [columnname thingsdone date]
  (when-not (first thingsdone)
    (do (println "Things done required when not using --view")
        (System/exit 0)))
  (pprint/cl-format true "I did this today: ~%~%~{* ~A~%~}~%" (dt-add-or-update-column    columnname thingsdone date)))



(defn -main
  "Record and view done things."
  [& args]
  (let [[options thingsdone banner] (cli args
                                         ["-r" "--report" "Report things done" :flag true]
                                         ["-v" "--view" "View things done" :flag true]
                                         ["-d" "--date" (str "The date you wish to view eg: " (lastmonth))]
                                         ["-y" "--yesterday" "change the date to yesterday" :flag true]
                                         ["-t" "--type" "Type of thing being added" :default "done" ]
                                         ["-h" "--help" "Show help" :default false :flag true]                                         
                                         )]
    (when (or (:help options) (not (or  (:view options) (first thingsdone) (:report options))))
      (println banner)
      (System/exit 0))
    (if (:report options)
      (do (println "Monthly Report")
      (view-month  (:type options) (get-date-from-options options (today)))))
    (if (:view options)
      (view-day (:type options) (get-date-from-options options (lastmonth))))
    (if (first thingsdone)
      (add-to-done-today  (:type options) thingsdone (get-date-from-options options  (today))))))
  


