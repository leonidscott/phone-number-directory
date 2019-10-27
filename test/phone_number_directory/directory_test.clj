(ns phone-number-directory.directory-test
  (:use midje.sweet)
  (:require [phone-number-directory.directory :as dir]
            [phone-number-directory.e164 :as e164]))

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

(fact "(insert-record!): new record tests"
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
                            => (swap! test-atom assoc new-pn [new-record])))))))

(def test-atom (atom directory-initial))

(fact "(insert-record!): new record tests"
      (with-redefs [dir/directory {}] ;to avoid importing the csv file
        (with-state-changes [(before :facts (reset! test-atom directory-initial))]
          (let [new-pn "+22222222222"
                new-record {:phone-number new-pn
                            :context "context2-0"
                            :name "name2-0"}]
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
                            => (swap! test-atom assoc new-pn [new-record])))))))

(fact "(insert-record!): identical record tests"
      (with-redefs [dir/directory {}]
        (with-state-changes [(before :facts (reset! test-atom directory-initial))]
          (let [identical-pn "+11111111111"
                identical-record {:phone-number identical-pn
                                  :context "context0"
                                  :name "name0"}]
            (fact "When a record is identical to one in directory,
                   directory will not be modified"
                  (do (dir/insert-record! identical-record)
                      @test-atom) => directory-initial
                  (provided (#'dir/deref-directory) => @test-atom))
            (fact "When a record is identical to one in directory,
                   directory will return that same record"
                  (dir/insert-record! identical-record) => identical-record
                  (provided (#'dir/deref-directory) => @test-atom))))))

(fact "(insert-record!): phone-number-context conflict tests"
      (with-redefs [dir/directory {}]
        (with-state-changes [(before :facts (reset! test-atom directory-initial))]
          (let [conflict-pn "+11111111111"
                conflict-record {:phone-number conflict-pn
                                 :context "context0"
                                 :name "name-conflict"}]
            (fact "When a record has a phone-number-context conflict,
                   directory will be unmodified"
                  (do (try
                        (dir/insert-record! conflict-record)
                        (catch Exception e))
                      @test-atom) => directory-initial
                  (provided (#'dir/deref-directory) => @test-atom))
            (fact "When a record has a phone-number context conflict,
                   insert-record! will throw an Exception"
                  (dir/insert-record! conflict-record)
                  => (throws Exception "phone-number-context-conflict")
                  (provided (#'dir/deref-directory) => @test-atom))))))

(fact "(insert-record!): regular insertion"
      (with-redefs [dir/directory {}]
        (with-state-changes [(before :facts (reset! test-atom directory-initial))]
          (let [existing-pn "+11111111111"
                new-context-record {:phone-number existing-pn
                                    :context "context2"
                                    :name "name2"}]
            (fact "When a record contains an existing pn, but a different context,
                   Directory will be updated with the new record"
                  (do (dir/insert-record! new-context-record)
                      @test-atom) => (update directory-initial
                                             existing-pn
                                             conj
                                             new-context-record)
                  (provided (#'dir/deref-directory) => @test-atom
                            (#'dir/swap-directory! update
                                                   existing-pn conj new-context-record)
                            => (swap! test-atom
                                      update existing-pn conj new-context-record)))
            (fact "When a record contains an existing pn, but a different context,
                   insert-record! will return that same record"
                  (dir/insert-record! new-context-record) => new-context-record
                  (provided (#'dir/deref-directory) => @test-atom
                            (#'dir/swap-directory! update
                                                   existing-pn conj new-context-record)
                            => (swap! test-atom
                                      update existing-pn conj new-context-record)))))))

(fact "(insert-record!): non e164-record tests"
      (with-redefs [dir/directory {}]
        (with-state-changes [(before :facts (reset! test-atom directory-initial))]
          (let [non-e164 "(111) 111-1111"
                non-e164-record {:phone-number non-e164
                                 :context "context2"
                                 :name "name2"}
                e164-pn (e164/convert non-e164)
                e164-record (assoc non-e164-record :phone-number e164-pn)]
            (fact "When a record contains a non-e164 phone-number,
                   and the record is added to directory,
                   directory will hold that record with an e164 phone-number"
                  (do (dir/insert-record! non-e164-record)
                      @test-atom) => (update directory-initial e164-pn conj e164-record)
                  (provided (#'dir/deref-directory) => @test-atom
                            (#'dir/swap-directory! update
                                                   e164-pn conj e164-record)
                            => (swap! test-atom
                                      update e164-pn conj e164-record)))
            (fact "When a record contains a non-e164 phone-number,
                   insert-record! will return that same record with an e164 pn"
                  (dir/insert-record! non-e164-record) => e164-record
                  (provided (#'dir/deref-directory) => @test-atom
                            (#'dir/swap-directory! update
                                                   e164-pn conj e164-record)
                            => (swap! test-atom
                                      update e164-pn conj e164-record)))))))
