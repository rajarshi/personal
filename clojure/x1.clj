(ns foo
  (:require [clojure.xml :as xml])
  (:require [clojure.zip :as zip])
  (:require [clojure.contrib.zip-filter.xml :as zf]))

;; read in reults xml
(def result (zip/xml-zip (xml/parse "entrez.xml")))

;; get id list
(zf/xml-> result :IdList :Id zf/text)
