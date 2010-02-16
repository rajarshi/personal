(ns entrez
  (:require [clojure.xml :as xml])
  (:require [clojure.zip :as zip])
  (:require [clojure.contrib.zip-filter.xml :as zf])
  (:require [clojure.contrib.duck-streams :as ds])
  (:require [clojure.contrib.str-utils2 :as str]))

(defn flatten
  "Flatten a list which may contain lists"
  [x]
  (let [s? #(instance? clojure.lang.Sequential %)]
    (filter
     (complement s?)
     (tree-seq s? seq x)))) 

(defn parse-str [s]
  (zip/xml-zip (xml/parse (new org.xml.sax.InputSource
			       (new java.io.StringReader s)))))  

(defn get-ids [zipper]
  (zf/xml-> zipper :IdList :Id zf/text))

(defn get-affiliations [zipper]
  (map (fn [x y] (list x y))
       (zf/xml-> zipper :PubmedArticle :MedlineCitation :PMID zf/text)
       (zf/xml-> zipper :PubmedArticle :MedlineCitation :Article :Affiliation zf/text)))

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

(defn efetch
  "Call the Entrez efetch service"
  [ids db]
  (ds/slurp* (str "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?"
		  "db=" db "&"
		  "id=" (str/join "," ids) "&"
		  "retmode=xml&"
		  "email=rajarshi.guha@gmail.com")))


;; FIPS country code related methods
(defn load-fips []
  "Read and parse FIPS country data, give back a list of 2-tuples of the form (CODE,name)"
  (map #(list (str/take (first %) 2) (str/lower-case (last %)))
       (map #(seq (.split (. % trim) "_"))
	    (filter #(str/contains? % "_country_")
		    (ds/read-lines "fips-414.txt")))))

(defn find-country-code 
  [s codes]
  (filter #(not (nil? %))
	  (map #(if (= (str/lower-case s) (last %))
		  (first %)
		  nil)
	       codes)))
(find-country-code "Italy" (load-fips))

;; split text on white space, remove stop words, for each
;; token query country collection in mongodb, return the hits
;; that we get
(def stop-words '("as" "is" "for" "in" 
		  "not" "the" "of" 
		  "college" "university" "institute" "corporation"))

(defn match-country 
  "Identify FIPS-1040 country code associated with a string"
  [s fips-country]
  (flatten (map #(find-country-code % fips-country) 
		(filter #(not (.contains stop-words (str/lower-case %))) 
			(seq (.split s "\\s+"))))))

(match-country "University of Perugia, Italy and Nigeria" (load-fips))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; read in reults xml
(def result (zip/xml-zip 
	     (xml/parse "/Users/guhar/src/personal/clojure/entrez.xml")))
 

;;  (count (esearch {:term "cheminformatics" :mindate "2009" :maxdate "2010"}))
(def ids (esearch {:term "2010[dp]" }))
(println (count ids))
(println (get-affiliations (parse-str (efetch ids "pubmed"))))

