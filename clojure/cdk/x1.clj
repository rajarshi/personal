(import '(org.openscience.cdk.smiles SmilesParser))
(import '(org.openscience.cdk DefaultChemObjectBuilder))
(import '(org.openscience.cdk.aromaticity CDKHueckelAromaticityDetector))
(import '(org.openscience.cdk.tools.manipulator AtomContainerManipulator))
(import '(org.openscience.cdk.fingerprint ExtendedFingerprinter)) 

(def dcob (. DefaultChemObjectBuilder (getInstance)))
(def sp (new SmilesParser dcob))

(defn rep [x n]
  (if (= 1 n) (list x)
      (concat (list x) (rep x (- n 1)))))

(defn prep-mol [x]
  ((. CDKHueckelAromaticityDetector (detectAromaticity x))
   (. AtomContainerManipulator (percieveAtomTypesAndConfigureAtoms x))))

(defn bfs [mol start path]
  ((if (= nil start) path)
   (concat path)))


(def smiles '("c1ccccc1CC(=O)CN", "CCC", "COCOC", "CCCCCC", "CCC", "c1ccccc1"))
(def smiles (reduce concat (for [x smiles] (rep x 100))))
(count smiles)

(def mols (for [x smiles] (. sp (parseSmiles x))))


(time (def junk (for [x mols] (. CDKHueckelAromaticityDetector (detectAromaticity x)))))
(time (def junk (for [x mols] (. AtomContainerManipulator (percieveAtomTypesAndConfigureAtoms x)))))


(for [x (. (first mols) atoms)] (. x (getAtomTypeName))) 

(for [x mols] 
  (for [atom (. x atoms)] 
    (. atom (getSymbol))))

(def fprinter (new ExtendedFingerprinter))
(time (def fps (for [x mols] (. fprinter (getFingerprint x)))))

;; count the number of elements in a list. Must be a better way!
(reduce (fn [x y] (+ x y)) (for [x smiles] 1))