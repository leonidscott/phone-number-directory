(ns phone-number-directory.directory-test
  (:use midje.sweet)
  (:require [phone-number-directory.directory :as dir]))

(def directory-initial {"+11111111111" [{:phone-number "+11111111111"
                                        :context "context0"
                                        :name "name0"}
                                       {:phone-number "+11111111111"
                                        :context "context1"
                                        :name "name1"}]})

(fact "phone-number->record returns a vector of maps (each representing a phone record) that corresponds to the phone-number"
      (with-redefs [dir/directory directory-initial]
        ;(println @#'dir/directory)
        (println (dir/phone-number->records "+11111111111"))
        ;=> (directory-initial "+11111111111")
        ))
