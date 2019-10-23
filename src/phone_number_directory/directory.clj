(ns phone-number-directory.directory
  (:require [phone-number-directory.csv-import :as csv]
            [phone-number-directory.e164 :as e164]))

(def ^:private directory (atom (csv/get-seed-data)))

(defn- deref-atom
  "For testing, the tests need to redirect any derefs of directory.
   Midje cannot mock deref, but it can mock this function"
  [atom]
  @atom)

(defn phone-number->records
  "Takes in a single phone-number (String),
   converts it to an e164 phone-number,
   and returns vector of maps, each representing a phone record
   If the phone-number is not present, returns []"
  [phone-number]
  ((deref-atom directory) (e164/convert phone-number)))
