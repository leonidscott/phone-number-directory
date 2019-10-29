(ns phone-number-directory.middleware
  (:require [clojure.data.json :as json]
            [phone-number-directory.directory :as dir]
            [phone-number-directory.e164 :as e164]))

(defn- json-marshaller
  "Takes the intended body of a responce in edn, and optionally, a http status
   returns json with a status and body"
  ([body]
   (json-marshaller 100 body))
  ([status body]
   (json/write-str {:status status
                    :body body})))

(defn- match-err-on-msg
  "err must be have a message key in its ex-data map.
   returns true if the err's message is the same as msg"
  [err msg]
  (= (:message (ex-data err)) msg))

(defn- e164-gate
  [phone-number]
  (if (e164/is-e164? phone-number)
    phone-number
    (throw (ex-info "not a e164 phone-number"
                    {:message (format "phone-number %s is not e-164" phone-number)}))))

;;; query-middleware functions
(defn- query-marshaller
  [results]
  (if results
    (json-marshaller {:results results})
    (throw (ex-info "no results exist for phone-number %s"
                    {:message "no results exist for phone-number %s"}))))

(defn query-middleware
  "implements query request. Returns results in http response ready json."
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

;;; number-middleware functions

(defn- unmarshal-number
  [request]
  (-> request
      :body
      slurp
      (json/read-str :key-fn keyword)
      :body))

(defn- nil-gate
  "are either number, context, or name nil?
   if so, throw err, if not, return record"
  [{:keys [number context name] :as record}]
  (if (and number context name)
    record
    (throw (ex-info "one or more record fields are nil"
                    {:message (str "one or more fileds in "
                                   (json/write-str record)
                                   " are nil")}))))

(defn- number->phone-number
  "takes a record map, converts number key to phone number key.
   returns a new record map"
  [{:keys [number context name]}]
  {:phone-number number :context context :name name})

(defn- pn-is-e164
  "is the phone-number key e164?
   if not, throw err, if so, return record"
  [{:keys [phone-number] :as record}]
  (e164-gate phone-number)
  record)

(defn number-middleware
  "handles number request, returns http ready json
   unmarshalls request, checks inputs, completes requests, and marshals results.
   handles errors, returns http ready json with error code and message"
  [request]
  (let [record (unmarshal-number request)
        nil-msg (str "one or more fileds in " (json/write-str record) " are nil")
        non-e164-msg (format "phone-number %s is not e-164" (:number record))]
    (try (-> record
             nil-gate
             number->phone-number
             pn-is-e164
             dir/insert-record!
             ((partial #(assoc {} :confirmation %)))
             json-marshaller)
         (catch Exception e
           (cond (match-err-on-msg e nil-msg)
                   (json-marshaller 400 {:error nil-msg})
                 (match-err-on-msg e non-e164-msg)
                   (json-marshaller 400 {:error non-e164-msg})
                 (match-err-on-msg e "phone-number-context-conflict")
                   (json-marshaller 400 {:error "phone-number-context-conflict"})
                 :else (throw e))))))
