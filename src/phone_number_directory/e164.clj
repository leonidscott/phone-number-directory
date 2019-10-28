(ns phone-number-directory.e164
  (:require [clojure.string :as str]))

(defn- strip
  "strip <string-chars> from <string>"
  [strip-chars string]
  (apply str (remove #((set strip-chars) %) string)))

(defn convert
  "Given either a conventionally written or e164 phone number,
   returns an e164 number.
   NOTE: Does not check that phone number format matches country code.
   NOTE: If no country code is provided, it adds a US +1 code."
  [phone-number]
  (let [stripped-phone-number (strip "()- " phone-number)]
    (if (str/includes? stripped-phone-number "+")
      stripped-phone-number
      (str "+1" stripped-phone-number))))

(defn is-e164?
  [str]
  (re-matches #"^\+?[1-9]\d{1,14}$" str))
