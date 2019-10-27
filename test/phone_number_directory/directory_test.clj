(ns phone-number-directory.directory-test
  (:use midje.sweet)
  (:require [phone-number-directory.directory :as dir]))

(def directory-initial {"+11111111111" [{:phone-number "+11111111111"
                                        :context "context0"
                                        :name "name0"}
                                       {:phone-number "+11111111111"
                                        :context "context1"
                                        :name "name1"}]})

;;;
;;phone-number->records tests
;;;

(fact "phone-number->record returns a vector of maps (each representing a phone record) that corresponds to the phone-number"
      (with-redefs [dir/directory directory-initial]
        (dir/phone-number->records "+11111111111") => (directory-initial "+11111111111")
        (provided (#'dir/deref-directory) => @#'dir/directory)))

(fact "phone-number->record corrects conventional phone numbers to e164"
      (with-redefs [dir/directory directory-initial]
        (dir/phone-number->records "(111) 111-1111")
          => (directory-initial "+11111111111")
        (provided (#'dir/deref-directory) => @#'dir/directory)))

(fact "phone-number->record returns nil when the record does not exist"
      (with-redefs [dir/directory directory-initial]
        (dir/phone-number->records "+22222222222") => nil
        (provided (#'dir/deref-directory) => @#'dir/directory)))

;;;
;;insert-record! tests
;;;

(with-redefs [dir/directory {}] ;to avoid importing the csv file
  (let [new-pn "+22222222222"
        new-record {:phone-number new-pn
                    :context "context2-0"
                    :name "name2-0"}
        test-atom (atom directory-initial)]
    (with-state-changes [(before :facts (reset! test-atom directory-initial))]
      (fact "When a record contains a previously unseen phone-number,
           insert-record! will add a new key value pair to directory"
            (do (dir/insert-record! new-record)
                @test-atom) => (assoc directory-initial new-pn [new-record])
            (provided (#'dir/deref-directory) => @test-atom
                      (#'dir/swap-directory! assoc new-pn [new-record])
                      => (swap! test-atom assoc new-pn [new-record])))
      (fact "When a record contains a previously unseen phone-number,
           insert-record! will return that same record"
            (dir/insert-record! new-record) => new-record
            (provided (#'dir/deref-directory) => @test-atom
                      (#'dir/swap-directory! assoc new-pn [new-record])
                      => (swap! test-atom assoc new-pn [new-record]))))))
