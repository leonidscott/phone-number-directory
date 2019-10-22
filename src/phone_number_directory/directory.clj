(ns phone-number-directory.directory
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defn- csv-data->maps
  "header is a vector of keywords
   [[line0], [line1], ... [line n]] ->
   ({:h0 line0val0, ..., :hn l0vn}, ..., {:h0 lnv0, ..., :hn lnvn})"
  [header csv-data]
  (map #(zipmap header %) csv-data))

(defn get-seed-data
  "Returns a map:
    {phone-number -> {:phone-number (E.164 Format), :context, :caller-id}}
   from file interview-callerid-data.csv.
   ASSUMPTION: There is a file called interview-callerid-data.csv in the the resources directory."
  []
  (->> (io/reader "resources/interview-callerid-data.csv")
       csv/read-csv ;Slurp CSV [[line0], [line1], ..., [line n]]
       (csv-data->maps [:phone-number :context :name])))
