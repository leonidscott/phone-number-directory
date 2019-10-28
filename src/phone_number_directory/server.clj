(ns phone-number-directory.server
  (:gen-class)
  (:require
    [compojure.core :refer [routes GET POST]]
    [compojure.route :as route]
    [environ.core :refer [env]]
    [hiccup.element :refer [javascript-tag]]
    [hiccup.page :refer [html5 include-css include-js]]
    [immutant.web :as immutant]
    [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
    [ring.middleware.reload :refer [wrap-reload]]
    [ring.middleware.webjars :refer [wrap-webjars]]
    [ring.util.response :refer [response]]
    [clojure.edn :as edn]
    [phone-number-directory.middleware :as middleware]))

(def app-routes
  (routes
    (GET "/query" {{phone-number :number} :params}
        (middleware/query-middleware phone-number))
    (POST "/number" request
          (println (middleware/number-middleware request)))
    (route/not-found "not found")))

(def handler
  (as-> app-routes h
        (if (:dev? env) (wrap-reload h) h)
        (wrap-defaults h (assoc-in site-defaults [:security :anti-forgery] false))
        (wrap-webjars h)))

(defn- read-config
  []
  (edn/read-string (slurp "config.edn")))

(defn run-server
  [port]
  (immutant/run handler {:port port}))

(defn -main
  [& args]
  (-> read-config
      :port
      run-server))
