(ns donetoday.core-test
  (:use midje.sweet)               ;; <<==
  (:require [donetoday.core :as core])
  (:require [clj-time.core :as time])
  )


(fact "truncate-time gives you a local time with day level resolution"
      (core/truncate-time  (time/date-time 1998 4 25 3 27 45) ) => (time/to-time-zone (time/date-time 1998 4 25)  (time/default-time-zone))
      (core/truncate-time  (time/local-date-time 1998 4 25 3 27 45) ) => (time/to-time-zone (time/date-time 1998 4 25) (time/default-time-zone))
      )


(fact "dyndb-today gives you a local time with day level resolution"
      (core/truncate-time  (time/date-time 1998 4 25 3 27 45) ) => (time/to-time-zone (time/date-time 1998 4 25)  (time/default-time-zone))
      (core/truncate-time  (time/local-date-time 1998 4 25 3 27 45) ) => (time/to-time-zone (time/date-time 1998 4 25) (time/default-time-zone))
      )

