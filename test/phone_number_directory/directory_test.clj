(ns phone-number-directory.directory-test
  (:use midje.sweet)
  (:require [phone-number-directory.directory :as dir]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

;;; Due to scoping, I can't mock the external libraries io/reader and csv/read-csv by dereffing @directory. However, @directory is initialized via get-seed-data. I can mock io and csv external calls from get-seed-data.
(fact "directory is a map of phone records indexed by phone number. (Important for fast lookup)"
      (let [csv-data [["+11111111111" "context0" "name0"]
                      ["+22222222222" "context1" "name1"]]
            indexed-map {"+11111111111" [{:phone-number "+11111111111"
                                          :context "context0"
                                          :name "name0"}]
                         "+22222222222" [{:phone-number "+22222222222"
                                          :context "context1"
                                          :name "name1"}]}]
        (#'dir/get-seed-data) => indexed-map
        (provided (#'io/reader "resources/interview-callerid-data.csv") => ..reader..
                  (#'csv/read-csv ..reader..) => csv-data)))

(fact "phone numbers from the csv that include parenthese, spaces, and dashes will have those characters removed"
      (let [csv-data [["+1 111 111 1111" "context0" "name0"]
                      ["+2 222 222-2222" "context1" "name1"]
                      ["+3 (333) 333-3333" "context2" "name2"]]
            indexed-map-keys '("+11111111111" "+22222222222" "+33333333333")]
        (keys (#'dir/get-seed-data)) => indexed-map-keys
        (provided (#'io/reader "resources/interview-callerid-data.csv") => ..reader..
                  (#'csv/read-csv ..reader..) => csv-data)))

(fact "records A and B containing the same phone number will be stored as follows: {phone-number -> [A B]}"
      (let [csv-data [["+11111111111" "context0" "name0"]
                      ["+11111111111" "context1" "name1"]]
            directory {"+11111111111" [{:phone-number "+11111111111"
                                        :context "context0"
                                        :name "name0"}
                                       {:phone-number "+11111111111"
                                        :context "context1"
                                        :name "name1"}]}]
        (#'dir/get-seed-data) => directory
        (provided (#'io/reader "resources/interview-callerid-data.csv") => ..reader..
                  (#'csv/read-csv ..reader..) => csv-data)))
