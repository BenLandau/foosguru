(ns foosguru.core
  (:gen-class))

(require '[clojure.pprint    :as p]
         '[clojure.java.jdbc :as j])

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

(defn -main
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



