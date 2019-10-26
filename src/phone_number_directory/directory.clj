(ns phone-number-directory.directory
  (:require [phone-number-directory.csv-import :as csv]
            [phone-number-directory.e164 :as e164]))

(def ^:private directory (atom (csv/get-seed-data)))

(defn- deref-atom
  "For testing, the tests need to redirect any derefs of directory.
   Midje cannot mock deref, but it can mock this function"
  [atom]
  @atom)

(defn phone-number->records
  "Takes in a single phone-number (String),
   converts it to an e164 phone-number,
   and returns vector of maps, each representing a phone record
   If the phone-number is not present, returns []"
  [phone-number]
  ((deref-atom directory) (e164/convert phone-number)))

;;;
;;start of insert-record! functions
;;;

(defn- directory-contains?
  "(record-filter) must take two record maps as arguments, and return a boolean.
   Checks if a (record-filter) relationsip exists between the inputed record,
   and any other record in atom, directory."
  [record-filter {:keys [phone-number] :as record}]
  (let [records (@directory phone-number)]
    (-> (filter #(record-filter record %) records)
        not-empty)))

(defn- new-phone-number?
  "Takes a record map (with an e164 phone-number)
   Returns true if the the record does not exist within directory, false if not"
  [{:keys [phone-number]}]
  (->> (@directory phone-number)
       not))

(defn- identical-record?
  [record1 record2]
  (= record1 record2))

(defn- pn-context-conflict?
  "Takes two record maps (with e164 records)"
  [{phone-number1 :phone-number context1 :context name1 :name}
   {phone-number2 :phone-number context2 :context name2 :name}]
  (and (= phone-number1 phone-number2)
       (= context1 context2)
       (not= name1 name2)))

(defn insert-record!
  [{:keys [phone-number context name] :as record}]
  (let [e164-pn (e164/convert phone-number)
        e164-record (assoc record :phone-number e164-pn)]
    (cond
      (new-phone-number? e164-record) (swap! directory assoc e164-pn [record])
      )))

;;;
;; end of insert-record! functions
;;;
