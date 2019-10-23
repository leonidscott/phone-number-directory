(ns phone-number-directory.directory
  (:require [phone-number-directory.csv-import :as csv]))

(def ^:private directory (atom (csv/get-seed-data)))
