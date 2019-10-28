(ns phone-number-directory.middleware-test
  (:use midje.sweet)
  (:require [clojure.data.json :as json]
            [phone-number-directory.directory :as dir]
            [phone-number-directory.middleware :as middle]))

;;; query-middleware tests
(fact "query-middleware will return status 400 when phone-number is not e164"
      (let [edn {:status 400
                 :body {:error ""}}
            non-e164-msg "phone-number %s is not e-164"
            affix-err (partial #(assoc-in edn [:body :error] (format non-e164-msg %)))]
        (tabular
         (fact (middle/query-middleware ?phone-number)
               => (json/write-str (affix-err ?phone-number)))
         ?phone-number
         "+1111111111111111" ;to long to be e164
         "abc"
         "")))

(fact "query-middleware will return status 404 when phone-number is not in directory"
      (let [pn "+11111111111"
            edn {:status 404
                 :body {:error (format "no results exist for phone-number %s" pn)}}
            json (json/write-str edn)]
        (middle/query-middleware pn) => json
        (provided (dir/phone-number->records pn) => nil)))

(fact "when phone-number is e164 and exists within phone-number directory,
       query-middleware will return json with status 100 and directory results"
      (let [pn "+11111111111"
            results [{:phone-number pn :context "c0" :name "n0"}]
            edn {:status 100
                 :body {:results results}}
            json (json/write-str edn)]
        (middle/query-middleware pn) => json
        (provided (dir/phone-number->records pn) => results)))


;;; number-middleware tests
(fact "number-middleware returns status 400 when any of the following records are nil:
       number, context, name"
      (let [record {:number "+5" :context "context"} ;missing name
            err-msg (str "one or more fileds in " (json/write-str record) " are nil")
            err-edn {:status 400
                     :body {:error err-msg}}
            err-json (json/write-str err-edn)]
        (middle/number-middleware anything) => err-json
        (provided (#'middle/unmarshal-number anything) => record)))

(fact "number-middleware returns status 400 when number is not a e164 phone-number"
      (let [record {:number "abc" :context "context" :name "name"}
            err-msg (format "phone-number %s is not e-164" (:number record))
            err-edn {:status 400
                     :body {:error err-msg}}
            err-json (json/write-str err-edn)]
        (middle/number-middleware anything) => err-json
        (provided (#'middle/unmarshal-number anything) => record)))

(fact "number-middleware returns status 400 when a record has a phone-number-context conflict"
      (let [err-msg "phone-number-context-conflict"
            err-edn {:status 400
                     :body {:error err-msg}}
            err-json (json/write-str err-edn)]
        (middle/number-middleware anything) => err-json
        (provided (#'middle/unmarshal-number anything)
                    => {:number "+11111111111" :context "context" :name "name"}
                  (dir/insert-record! anything)
                    => (throw (ex-info err-msg {:message err-msg})))))

(fact "number-middleware returns state 100 and the inserted record
       when record does not have nil number, context, or name,
       has valid e164 phone number,
       and no phone-number-context conflict"
      (let [record {:number "+11111111111" :context "context" :name "name"}
            return-edn {:status 100
                        :body {:confirmation record}}
            return-json (json/write-str return-edn)]
        (middle/number-middleware anything) => return-json
        (provided (#'middle/unmarshal-number anything) => record
                  (dir/insert-record! anything) => record)))
