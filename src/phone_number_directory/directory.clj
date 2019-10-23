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

(defn- record-new?
  [record]
  (->> (:phone-number record)
       @directory
       not))

(defn- identical-record?
  [record]
  (let [records (@directory (:phone-number record))]
    (not-empty (filter #(= % record) records))))

(defn- name-context-conflict?
  [{phone-number1 :phone-number context1 :context name1 :name}
   {phone-number2 :phone-number context2 :context name2 :name}]
  (and (= phone-number1 phone-number2)
       (= context1 context2)
       (not= name1 name2)))

(defn insert-record!
  [{:keys [phone-number context name] :as record}]
  (let [e164-pn (e164/convert phone-number)]
    (cond
      (record-new? e164-pn) (swap! directory assoc e164-pn (vec record))
      )))
