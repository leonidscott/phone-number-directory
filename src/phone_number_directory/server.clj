(ns phone-number-directory.server
  (:gen-class)
  (:require
    [compojure.core :refer [routes GET]]
    [compojure.route :as route]
    [environ.core :refer [env]]
    [hiccup.element :refer [javascript-tag]]
    [hiccup.page :refer [html5 include-css include-js]]
    [immutant.web :as immutant]
    [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
    [ring.middleware.reload :refer [wrap-reload]]
    [ring.middleware.webjars :refer [wrap-webjars]]
    [ring.util.response :refer [response]]
    [phone-number-directory.directory :as directory]))

(defn render-home-page
  []
  (html5
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:http-equiv "X-UA-Compatible" :content "IE-edge"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:title "phone-number-directory :: Home Page"]
     (include-css "/assets/bootstrap/css/bootstrap.min.css")
     (include-css "css/app.css")
     (include-js "cljs/app.js")]
    [:body
     [:div#app [:h1 "Waiting for ClojureScript to load ..."]]
     (javascript-tag "phone_number_directory.client.run();")]))

(def app-routes
  (routes
    (GET "/" [] (render-home-page))
    (route/not-found "not found")))

(def handler
  (as-> app-routes h
        (if (:dev? env) (wrap-reload h) h)
        (wrap-defaults h (assoc-in site-defaults [:security :anti-forgery] false))
        (wrap-webjars h)))

(defn run-server
  []
  (immutant/run handler {:port 8080}))

;(defn -main
;  [& args]
                                        ;  (run-server))

(defn -main
  [& args]
  (println (directory/func "a")))
