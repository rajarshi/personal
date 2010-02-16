(import '(org.openscience.cdk.smiles SmilesParser))
(import '(org.openscience.cdk DefaultChemObjectBuilder))
(import '(org.openscience.cdk.aromaticity CDKHueckelAromaticityDetector))
(import '(org.openscience.cdk.tools.manipulator AtomContainerManipulator))
(import '(org.openscience.cdk.fingerprint ExtendedFingerprinter)) 
(import '(org.openscience.cdk CDKConstants)) 
(import '(org.openscience.cdk.fingerprint MACCSFingerprinter)) 

;; so we can read lines from a file
(use 'clojure.contrib.duck-streams)

(def dcob (. DefaultChemObjectBuilder (getInstance)))
(def sp (new SmilesParser dcob))

;; read in our SMILES
(def smiles (map (fn [x] (. x trim)) (read-lines "pubchem.smi") ) )
(println (concat (list "Got" (count smiles) "smiles")))

;; parse them 
(def mols (map (fn [x] (. sp (parseSmiles x))) smiles))
(println (concat (list "Got " (count mols)
		       "molecules")))
;; lets do some stress testing
;; make a replicate method
(defn rep [x n]
  (if (= 1 n) (list x)
      (concat (list x) (rep x (- n 1)))))

;; replicate the SMILES list
;; (def smiles (reduce concat (for [x smiles] (rep x 100))))
;; (println (count smiles))

;; now parse this list
(def mols (for [x smiles] (. sp (parseSmiles x))))

;; how long does this take?
(time (def mols (for [x smiles] (. sp (parseSmiles x)))))

(defn isaromatic? [mol] (some (fn [x] 
				(. x (getFlag (. org.openscience.cdk.CDKConstants ISAROMATIC))))
			      (. mol atoms)))
;; (println (for [x mols] (isaromatic? x)))

(def pol (new org.openscience.cdk.charges.Polarizability))

(def fprinter (new MACCSFingerprinter))

(time (def junk (map (fn [x] (. fprinter (getFingerprint x))) mols)))
(time (def junk (pmap (fn [x] (. fprinter (getFingerprint x))) mols)))


;; (time (def junk (map (fn [x] (. CDKHueckelAromaticityDetector (detectAromaticity x))) mols)))
;; (time (def junk (pmap (fn [x] (. CDKHueckelAromaticityDetector (detectAromaticity x))) mols)))

;; (time (def junk (map (fn [x] (. pol (calculateKJMeanMolecularPolarizability x))) mols)))
;; (time (def junk (pmap (fn [x] (. pol (calculateKJMeanMolecularPolarizability x))) mols)))

;; (time (def junk (map (fn [x] (isaromatic? x)) mols)))
;; (time (def junk (pmap (fn [x] (isaromatic? x)) mols)))

(shutdown-agents)
;; see which mol was aromatic
;; (def aromatics (for [x mols :when (isaromatic? x)] (x)))
