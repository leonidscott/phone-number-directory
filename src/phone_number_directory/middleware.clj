(ns phone-number-directory.middleware
  (:require [clojure.data.json :as json]
            [phone-number-directory.directory :as dir]
            [phone-number-directory.e164 :as e164]))

(defn- json-marshaller
  ([body]
   (json-marshaller 100 body))
  ([status body]
   (json/write-str {:status status
                    :body body})))

(defn- match-err-on-msg
  [exp msg]
  (= (:message (ex-data exp)) msg))

(defn- e164-gate
  [phone-number]
  (if (e164/is-e164? phone-number)
    phone-number
    (throw (ex-info "not a e164 phone-number"
                    {:message (format "phone-number %s is not e-164" phone-number)}))))

(defn- query-marshaller
  [results]
  (if results
    (json-marshaller {:results results})
    (throw (ex-info "no results exist for phone-number %s"
                    {:message "no results exist for phone-number %s"}))))

(defn query-middleware
  [phone-number]
  (try
    (-> phone-number
        e164-gate
        dir/phone-number->records
        query-marshaller)
    (catch Exception e
      (let [non-164-msg (format "phone-number %s is not e-164" phone-number)
            no-results-msg "no results exist for phone-number %s"]
        (cond (match-err-on-msg e non-164-msg)
                (json-marshaller 400 {:error non-164-msg})
              (match-err-on-msg e no-results-msg)
                (json-marshaller 404 {:error (format no-results-msg phone-number)})
              :else (throw e))))))
