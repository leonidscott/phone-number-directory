(ns phone-number-directory.directory
  (:require [phone-number-directory.csv-import :as csv]
            [phone-number-directory.e164 :as e164]))

(def ^:private directory (atom (csv/get-seed-data)))

(defn phone-number->records
  "Takes in a single phone-number (String),
   converts it to an e164 phone-number,
   and returns vector of maps, each representing a phone record
   If the phone-number is not present, returns []"
  [phone-number]
  (@directory (e164/convert phone-number)))
