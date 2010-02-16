

(def mol (getmol "C(O)(F)CN"))
(def mol (getmol "C(Br)(OS)[Si]N(Cl)(F)"))

(def fatom (first (. mol atoms)))
(def latom (last (. mol atoms)))



(defn get-shells 
  "Get the atoms in successive shells surrounding a specified atom. The first shell equals the immediate neighbors, the second shell the neighbors at bond length 2 and so on. The return value is a list in which each element is a list of atoms. The first element of the top level list is the n'th shell. Note that the return value does not contain the starting atom (i.e., zeroth shell"
  [mol start depth]
  (loop [shells (list (list start))
	 visited (list)]
    (if (= (count shells) (+ 1 depth))
      (drop-last shells)
      (recur (conj shells 
		   (filter #(not (has visited %)) 
			   (flatten (map #(get-connected-atoms-list mol %) (first shells)))))
	     (set (flatten (list visited shells)))))))



(get-shells mol fatom 1)

(defn sym [atoms] (map #(. % getSymbol) atoms))
(defn atype [atoms] 
  (map #(. % getAtomTypeName)
       atoms))

(defn make-atom-fp 
  [mol atom depth]
  (map #(str-join "-" (sort %)) 
       (map atype
	    (get-shells mol atom depth))))



(def mol (getmol "C(O)(F)CN"))
(map #(str-join "-" %) (map #(make-atom-fp mol % 2) (get-atom-list mol)))