(ns phone-number-directory.middleware-test
  (:use midje.sweet)
  (:require [clojure.data.json :as json]
            [phone-number-directory.directory :as dir]
            [phone-number-directory.middleware :as middle]))

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
