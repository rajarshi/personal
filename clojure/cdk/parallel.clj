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


(defn getmol [smiles] (. sp (parseSmiles smiles)))
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

;; # is shorthand for an anonymous function and
;; % is its argument
(def mols (doall (map #(getmol (. % trim))
		      (read-lines "junk.smi"))))

(def matches 
     (map (fn [x]
	    (. sqt (matches x))) 
	  mols))

(def fpmatches 
     (map (fn [x] 
	    (issubstruct? 
	     queryfp (getfp x))) 
	  mols))

;; some timing experiments
;; we use the doall form to force evaluation of the list

;; 28102 on gf13
(time (def junk1 (doall (map getfp mols)))) ;; 40248, 39292, 38856, 37949

;; 8693 on gf13
(time (def junk (doall (pmap getfp mols)))) ;; 23276, 24999, 23536


(count (filter #(if (not %) %)
	       (map 
		(fn [x,y] 
		    (. x (equals y)))
		fpserial fpparallel)))

(time (def mol1 (doall (pmap #(getmol (. % trim))
		     lines))))
