(ns phone-number-directory.csv-import
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [phone-number-directory.e164 :as e164]))

(defn- csv-data->maps
  "header is a vector of keywords [:h0 ... :hn]
   [[line0], [line1], ... [line n]] ->
   ({:h0 line0val0, ..., :hn l0vn}, ..., {:h0 lnv0, ..., :hn lnvn})"
  [header csv-data]
  (map #(zipmap header %) csv-data))

(defn get-seed-data
  "Returns a map:
    {phone-number -> {:phone-number (E.164 Format), :context, :caller-id}}
   from file interview-callerid-data.csv.
   ASSUMPTION: There is a file called interview-callerid-data.csv resources/"
  []
  (->> (io/reader "resources/interview-callerid-data.csv")
       csv/read-csv ;Slurp CSV [[line0], [line1], ..., [line n]]
       (csv-data->maps [:phone-number :context :name])
       (map #(update % :phone-number e164/convert)) ;convert phone-numbers to e164
       (group-by :phone-number)))
