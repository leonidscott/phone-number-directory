(ns phone-number-directory.client
  (:require
    [reagent.core :as r]))

(defn main-app-component
  []
  [:h1 "Hello, world!"])

(defn reload
  []
  (r/render-component [main-app-component] (.getElementById js/document "app")))

(defn ^:export run
  []
  (enable-console-print!)
  (reload))
