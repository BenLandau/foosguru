(ns foosguru.core
  (:require [org.httpkit.server :as s]
            [clojure.pprint     :as p]
            [compojure.core     :refer :all]
            [compojure.route    :refer :all]
            [clojure.java.jdbc  :as j])
  (:gen-class))
(use '[ring.middleware.json :only [wrap-json-body wrap-json-response]]
     '[ring.util.response   :only [response]])


(declare
  insert-score-interactive
  digest-score-interactive
  max-gid)

  
;;; Accept new result 

;; Player 1
;; Player 1 score
;; Player 2
;; Player 2 score
;; Date


(defn map-scores
  ([date p1 s1 p2 s2]
    {:date       date
      :red-name   p1
      :red-score  s1
      :blue-name  p2
      :blue-score s2})
  ([p1 s1 p2 s2]
    { :red-name   p1
       :red-score  s1
       :blue-name  p2
       :blue-score s2}))
  
(defn digest-score-interactive 
    "Function to take score from repl and append to state"
    []
  (let [raw (read-line)] 
      (apply map-scores 
        (clojure.string/split 
          raw 
          #"[, ]"))))
;; webserver
;; db connection


(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "src/foosguru/foosdata"})

(defn date->str
  [dt]
  (.format 
    (java.text.SimpleDateFormat. "yyyy-MM-dd")
    dt))

(defn get-today-date
  []
  (date->str 
    (new java.util.Date)))


(defn insert-singles-record 
  ([date, p1, p1s, p2, p2s]
    (j/insert! db 
              :Singles 
              {:GID         (+ (max-gid) 1) 
               :Date        date
               :Red_Name    p1
               :Red_Score   p1s 
               :Blue_Name   p2 
               :Blue_Score  p2s}))
  ([p1, p1s, p2, p2s]
    (j/insert! db 
              :Singles 
              {:GID         (+ (max-gid) 1) 
               :Date        (get-today-date)
               :Red_Name    p1
               :Red_Score   p1s 
               :Blue_Name   p2 
               :Blue_Score  p2s})))


(defn max-gid
  []
  (let 
    [{old :old} (first (j/query db "SELECT max(GID) as old from Singles;"))]
    (if (nil? old)
      0 
      old)))


(defn mainfn
  []
  (do 
    (def state [])
    (while (empty? []) 
      (let [x (digest-score-interactive)]
        (if (= (count x) 4)
          (let [{:keys [red-name red-score blue-name blue-score]} x]
            (insert-singles-record red-name red-score blue-name blue-score))
          (if (= (count x) 5)
            (let [{:keys [date red-name red-score blue-name blue-score]} x]
              (insert-singles-record date red-name red-score blue-name blue-score))
            ("Incorrect format")))))))

(defroutes app
  (POST "/score" req
    ;;(str (:p1 req) " " (:p2 req) " " (:s1 req) " " (:s2 req)))
    (let [{:keys [p1 s1 p2 s2]} (:body req)]
      (insert-singles-record p1 s1 p2 s2))
    (response
      (interleave
        (map 
          #(get 
             (:body req) %) 
             [:p1 :p2 :s1 :s2])
        (repeat " ")))) 
  (GET "/" [] (str "Hello World")))


(defn -main [& args]
  (s/run-server (wrap-json-body app {:keywords? true}) {:port 8080})
  (println "Server started on port 8080"))
