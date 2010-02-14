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

(defn proc-date 
  "If argument is a string return a CGI query term, otherwise empty string"
  [name value]
  (if (= 0 (count value))
    (str "")
    (str name "=" value "&")))

;; The :keys is a shortcut, to make the keys of the map the names of the
;; local variable. The :or key provides default values. We provide a 
;; pre-condition to ensure that we have a term to search on
(defn esearch 
  "Perform an Entrez search. By default the database searched in PubMed. By default returns 1000000 ids that can be used in efetch. The :term keyword must be specified. All others are optional"
  [{:keys [term db retmode retmax mindate maxdate],
    :or {db "pubmed", retmode "xml", mindate "", maxdate "", retmax 1000000}}]
  {:pre [(not (nil? term))]}
  (get-ids (parse-str (ds/slurp* 
		       (str "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?"
			    "db=" db "&"
			    "term=" term "&"
			    "retmode=" retmode "&"
			    "retmax=" retmax "&"
			    (proc-date "mindate" mindate)
			    (proc-date "maxdate" maxdate)
			    "email=rajarshi.guha@gmail.com")))))

  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; read in reults xml
(def result (zip/xml-zip 
	     (xml/parse "/Users/guhar/src/personal/clojure/entrez.xml")))
 

(count (esearch {:term "cheminformatics" :mindate "2009" :maxdate "2010"}))
(count (esearch {:term "cheminformatics" }))

