(import '(org.openscience.cdk.smiles SmilesParser))
(import '(org.openscience.cdk DefaultChemObjectBuilder))
(import '(org.openscience.cdk.fingerprint MACCSFingerprinter)) 
(import '(org.openscience.cdk.fingerprint Fingerprinter)) 
(import '(org.openscience.cdk.fingerprint ExtendedFingerprinter)) 
(import '(org.openscience.cdk.smiles.smarts SMARTSQueryTool))
;; so we can read lines from a file
(use 'clojure.contrib.duck-streams)

(def sp (new SmilesParser (. DefaultChemObjectBuilder (getInstance))))
(def fprinter (new ExtendedFingerprinter))


(defn getfp [mol] (. fprinter (getFingerprint mol)))

(defn issubstruct? [query,target] 
  (let [x (. target (and query))]
    (. target (equals query))))

;; (def querySmiles "C(C)(C)OCC(C)=C")
(def querySmiles "Nc1ccc(CC)cc1")
(def querySmiles "O1CCC(O)CCCC1")
(def sqt (new SMARTSQueryTool querySmiles))
(def queryfp (getfp (getmol querySmiles)))
(println queryfp)

(def mols 
     (map (fn [x] 
	    (getmol (. x trim))) 
	  (read-lines "junk.smi")))

(def matches 
     (map (fn [x]
	    (. sqt (matches x))) 
	  mols))

(def fpmatches 
     (map (fn [x] 
	    (issubstruct? 
	     queryfp (getfp x))) 
	  mols))