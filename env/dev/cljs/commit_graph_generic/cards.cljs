(ns commit-graph-generic.cards
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [cljs-time.core :as time]
            [commit-graph-generic.core :as core]
            [cljs-time.core :as time]
            [cljs-time.coerce :as coerce]
            [commit-graph-generic.time :refer [past-year-since]]
            )
  (:require-macros
   [cljs.core.async.macros :refer [go]]
   [devcards.core
    :as dc
    :refer [defcard defcard-doc defcard-rg deftest]]))


;; --------- fetch some example api responses -------

(defn api-responses-channel [type t0 t1]
  (http/get
   (str "http://verdigris.ischool.berkeley.edu:9998/query/" type)
   {:with-credentials? false
    :query-params {"t0" t0 "t1" t1}
    }))

(def response-chan
  (api-responses-channel "sessions" 1465104650.532 1465799736.064)
  )
;; ------------ bucket datapoints by time -------------

(defn time-of [observation]
  (time/local-date-time
   (coerce/from-string
    (:timestamp observation))))

(defn dated [api-data]
   "Produces [  {:local-date-time :observation {:type :timestamp :id}} ...]"
  (->> api-data
       (sort-by :timestamp)
       (map #(hash-map :local-date-time (time-of %) :observation %))
       ))

(defn same-day? [dt1 dt2]
  (time/equal?
   (time/at-midnight dt1)
   (time/at-midnight dt2)))

(defn occurred-on [date responses]
  (filter
   #(same-day? date (:local-date-time %))
   responses))

(defn today []
  (time/local-date-time (time/now)))

(defn calendar-of [observations]
  "Returns a list
   [ {:date
      :events [{:local-date-time :observation} ... ]
      }
   ]"
  (let [past-year (past-year-since (today))
        dated-observations (dated observations)]
    (for [day past-year]
      (hash-map :date day
                :events (occurred-on day dated-observations))
      )
    )
  )

;; ------------------  get the data! ----------------

(defonce calendar-observations-atom (atom []))

(go (let [api-response (<! response-chan)]
      (reset! calendar-observations-atom
              (calendar-of (:body api-response)))))

;; ------------- views ------------------------------

(defn stringify [clj-data]
  (let [js-data (clj->js clj-data)]
    (.stringify js/JSON js-data nil 2)))

(defn view-json [json];type t0 t1]
  [:div
   [:pre
    (stringify json)
    ]])

(defn square [color size x y]
  (let [margin (/ size 10)
        square-size (- size margin)
        px #(str % "px")
        slot #(* (+ margin square-size) %)
        ]
    [:div
     {:style
      {:background-color color
       :position "absolute"
       :left (px (slot x))
       :top (px (slot y))
       :margin (px margin)
       :width (px square-size)
       :height (px square-size)}}]))

(defn day-square [index day]
  (let [x (.floor js/Math (/ index 7))
        y (mod index 7)
        color (if (zero? (count (:events day)))
                "#ddd"
                "#0f0")
        ]
    (square color 10 x y))
  )

(defn plot-from [days]
  (let [square-size 10]
    [:div
     {:style {:height (* 7 square-size)
              :padding "20px"
              }}
      (map-indexed day-square days)
     ]
    )
  )

;; ------------- cards ------------------------------
(defcard-rg plot
  (plot-from @calendar-observations-atom)
  )

(defcard-rg square
  [:div
   {:style {:width "50px" :height "100px"}}
   (square "#ddd" 10 4 4)
  ])

(defcard-rg api-responses-card
  (view-json
   (last @calendar-observations-atom
    )
   ))

;;(defcard-rg plot
;;  (plot-from (time/date-time 2016 5 30)))

(reagent/render [:div] (.getElementById js/document "app"))

;; remember to run 'lein figwheel devcards' and then browse to
;; http://localhost:3449/cards
