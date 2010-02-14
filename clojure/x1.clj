(ns foo
  (:require [clojure.xml :as xml])
  (:require [clojure.zip :as zip])
  (:require [clojure.contrib.zip-filter.xml :as zf])
  (:require [clojure.contrib.duck-streams :as ds]))

(defn parse-str [s]
  (zip/xml-zip (xml/parse (new org.xml.sax.InputSource
			       (new java.io.StringReader s)))))  

(defn get-ids [zipper]
  (zf/xml-> zipper :IdList :Id zf/text))

;; The :keys is a shortcut, to make the keys of the map the names of the
;; local variable. The :or key provides default values. We provide a 
;; pre-condition to ensure that we have a term to search on
(defn esearch [{:keys [term db retmode], :or {db "pubmed", retmode "xml"}}]
  {:pre [(not (nil? term))]}
  (list db term retmode))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; read in reults xml
(def result (zip/xml-zip 
	     (xml/parse "/Users/guhar/src/personal/clojure/entrez.xml")))

;; get id list
(zf/xml-> result :IdList :Id zf/text)


(def url1 "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=cancer&reldate=60&datetype=edat&retmax=100&usehistory=y")

;; get xml
(get-ids (parse-str (ds/slurp* url1)))



(esearch {:db "pubmed"})