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

;;; query-middleware functions
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

;;; number-middleware functions

(defn- unmarshal-number
  [request]
  (-> request
      :body
      slurp
      (json/read-str :key-fn keyword)
      :body))

(defn- nil-gate
  [{:keys [number context name] :as record}]
  (if (and number context name)
    record
    (throw (ex-info "one or more record fields are nil"
                    {:message (str "one or more fileds in "
                                   (json/write-str record)
                                   " are nil")}))))

(defn- number->phone-number
  [{:keys [number context name]}]
  {:phone-number number :context context :name name})

(defn- pn-is-e164
  [{:keys [phone-number] :as record}]
  (e164-gate phone-number)
  record)

(defn number-middleware
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
