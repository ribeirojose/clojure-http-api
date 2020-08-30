(ns hello
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

; HTTP responses

(defn ok [body]
  {:status 200 :body body})

(defn not-found [] 
  {:status 404 :body "Not found.\n"})

; GET /greet 

(def unmentionables #{"Voldemort" "曹操"})

(defn greeting-mentionables [nm]
  (if (unmentionables nm) 
    (throw (AssertionError. "Unmentionable name provided as input."))
    (str "Hello, " nm "!\n")))

(defn greet-request [request] 
  (let [nm (get-in request [:query-params :name] "world")]
    (greeting-mentionables nm)))


(defn respond-hello [request]
  (try  
    (ok (greet-request request))
    (catch AssertionError e 
      (println  (.getMessage e))
      (not-found))))
  

; service definitions

(def routes
  (route/expand-routes
                       #{["/greet" :get respond-hello :route-name :greet]}))

(def service-map
  {::http/routes routes
   ::http/type   :jetty
   ::http/port   8890})

; server configuration

(defn start []
  (http/start (http/create-server service-map)))

                                                                                        ;; For interactive development
(defonce server (atom nil))                                                             

(defn start-dev []
  (reset! server                                                                        
          (http/start (http/create-server
                       (assoc service-map
                              ::http/join? false)))))                                   

(defn stop-dev []
  (http/stop @server))

(defn restart []                                                                       
  (stop-dev)
  (start-dev)) 
