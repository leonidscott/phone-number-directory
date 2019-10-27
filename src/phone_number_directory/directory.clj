(ns phone-number-directory.directory
  (:require [phone-number-directory.csv-import :as csv]
            [phone-number-directory.e164 :as e164]))

(def ^:private directory (atom (csv/get-seed-data)))

;;; directory interfaces. Needed for testing
(defn- deref-directory
  "For testing, the tests need to redirect any derefs of directory.
   Midje cannot mock deref, but it can mock this function"
  []
  @directory)

(defn- swap-directory!
  [f & args]
  (apply swap! directory f args))


;;; phone-number->records
(defn phone-number->records
  "Takes in a single phone-number (String),
   converts it to an e164 phone-number,
   and returns vector of maps, each representing a phone record
   If the phone-number is not present, returns []"
  [phone-number]
  ((deref-directory) (e164/convert phone-number)))

;;;
;;start of insert-record! functions
;;;

(defn- directory-contains?
  "Does the directory contain (record-filter) relation to the inputed record?
   Note: (record-filter) must take two record maps as arguments, and return a boolean."
  [record-filter {:keys [phone-number] :as record}]
  (let [records ((deref-directory) phone-number)]
    (-> (filter #(record-filter record %) records)
        not-empty)))

(defn- new-phone-number?
  "Takes a record map (with an e164 phone-number)
   Does this phone-number exists within directory?"
  [{:keys [phone-number]}]
   (->> ((deref-directory) phone-number)
        not))

(defn- identical-record?
  "Are these two record maps identical?"
  [record1 record2]
  (= record1 record2))

(defn- pn-context-conflict?
  "Takes two record maps (with e164 records)
   A phone-number context conflict exists when two record maps share
   the same phone number and context, but different names"
  [{phone-number1 :phone-number context1 :context name1 :name}
   {phone-number2 :phone-number context2 :context name2 :name}]
  (and (= phone-number1 phone-number2)
       (= context1 context2)
       (not= name1 name2)))

(defn insert-record!
  [{:keys [phone-number context name] :as record}]
  (let [e164-pn (e164/convert phone-number)
        e164-record (assoc record :phone-number e164-pn)
        record-has? (partial #(directory-contains? % e164-record))]
    (cond
      (new-phone-number? e164-record) (swap-directory! assoc e164-pn [record])
      (record-has? identical-record?) nil
      (record-has? pn-context-conflict?)
        (throw (Exception. "phone-number-context-conflict"))
      :else (swap-directory! update e164-pn conj e164-record))
    e164-record))

;;;
;; end of insert-record! functions
;;;

(defn insert-record-2!
  [{:keys [phone-number context name] :as record}]
  (let [e164-pn (e164/convert phone-number)
        e164-record (assoc record :phone-number e164-pn)
        record-has? (partial #(directory-contains? % e164-record))]
    (cond
      (new-phone-number? e164-record) (swap-directory! assoc e164-pn [record])
      (record-has? identical-record?) nil
      (record-has? pn-context-conflict?)
        (throw (Exception. "phone-number-context-conflict"))
      :else (swap-directory! update e164-pn conj e164-record)
    )
    e164-record))
