(ns phone-number-directory.directory
  (:require [phone-number-directory.csv-import :as csv]))

(def ^:private directory (atom (csv/get-seed-data)))

(defn phone-number->records
  "Takes in a single phone-number (String),
   Returns vector of maps, each representing a phone record
   If the phone-number is not present, returns []"
  [phone-number])
