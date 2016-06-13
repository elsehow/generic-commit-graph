(ns commit-graph-generic.prod
  (:require [commit-graph-generic.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
