(ns commit-graph-generic.time
  (:require
   [cljs-time.core :as time]
   ;[cljs-time.format :as format]
   [cljs-time.periodic :refer [periodic-seq]]
   [cljs-time.predicates :as predicates]))

(defn find-next-sunday [date]
  "Finds closest Sunday after date"
  (if (predicates/sunday? date) date
      (recur (time/plus date (time/days 1)))
      ))

(defn past-year-since [date]
  "Returns a lazy sequence of dates, starting a year before the given date (sequence always begins on a Sunday)"
  (let [year-ago (time/minus date (time/years 1))
        year-ago-sunday (find-next-sunday year-ago)]
    (periodic-seq
     year-ago-sunday
     date
     (time/days 1))
  ))

;;;; testing
;;(defn format [date]
;;  (let [formatter (format/formatter "yyyy MM dd")]
;;    (format/unparse formatter date)
;;    ))
;;(println
;;
;; (map
;;  #(str (format %) "\n")
;;  (time-utils/date-seq (time/date-time 2016 05 30))
;;  )
;;
;; )
