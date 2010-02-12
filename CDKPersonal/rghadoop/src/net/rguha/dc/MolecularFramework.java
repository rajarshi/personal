// $Id: MolecularFramework.java 3984 2010-02-01 19:45:19Z nguyenda $
// class to generate molecular/Murcko framework

package gov.nih.ncgc.descriptor;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import chemaxon.struc.MolAtom;
import chemaxon.struc.MolBond;
import chemaxon.struc.Molecule;
import chemaxon.struc.RxnMolecule;
import chemaxon.formats.MolImporter;
import chemaxon.sss.screen.HashCode;
import chemaxon.util.MolHandler;

import chemaxon.reaction.Standardizer;
import chemaxon.reaction.Recap;
import chemaxon.reaction.ExhaustiveFragmenter;

import gov.nih.ncgc.util.MolStandardizer;
import gov.nih.ncgc.util.UnionFind;
import gov.nih.ncgc.util.GrayCode;


public class MolecularFramework {
    static private boolean debug = Boolean.getBoolean("framework.debug");
    static private Logger logger = Logger.getLogger
	(MolecularFramework.class.getName());

    public static enum FragmentType {
	FRAG_MURCKO,
	    FRAG_CARBON,
	    FRAG_RECAP
	    }

    static final String[] RECAP_REACTIONS = {
	"[O:3]=[C!$(C([#7])(=O)[!#1!#6]):2]-[#7!$([#7][!#1!#6]):1]>>[O:3]=[C:2].[#7:1]", // amide
	"[#6!$([#6](O)~[!#1!#6])][O:2][C:1]=O>>[C:1]=O.[#6][O:2]", // ester
	"[#6:2]-[N!$(N[#6]=[!#6])!$(N~[!#1!#6])!X4:1]>>[N:1].[#6:2]", // amine
	"N[C:1]([N:2])=O>>N[C:1]=O.[N:2]", // urea
	"[#6]-[O!$(O[#6]~[!#1!#6]):1]-[#6:2]>>[#6:2].[O:1]-[#6]", // ether
	"[C:1]=[C:1]>>[C:1].[C:1]", // olefin
	"[#6:1]-[N$(N([#6])([#6])([#6])[#6])!$(NC=[!#6]):2]>>[#6:1].[N:2]", // quatN
	"[n:1]-[#6!$([#6]=[!#6]):2]>>[n:1].[#6:2]", // aromN-carbon
	"[C:3](=[O:4])@-[N:1]!@-[#6!$([#6]=[!#6]):2]>>[C:3](=[O:4])[N:1].[#6:2]", // lactamN-carbon
	"[c:1]-[c:1]>>[c:1].[c:1]", // aromc-aromc
	"[#7:1][S:2](=O)=O>>[#7:1].[S:2](=O)=O" // sulphonamide
    };

    static final String[] RECAP_NOTLIST = {
	"CCCC", // butyl
	"CC(C)C", // ibutyl
	"C(C)(C)C", // tbutyl
	"C1=CC=CC=C1" // benzol
    };


    private Molecule mol;
    private FragmentType type;

    private HashCode hash = new HashCode ();
    private boolean generateAtomMapping = false;
    private MolStandardizer standardizer;
    private Set<String> notList = new HashSet<String>();

    // for FRAG_RECAP
    private ExhaustiveFragmenter exfrag;

    // parameters relevant to FRAG_MURCKO & FRAG_CABRON
    private int maxNumRings = 10;
    private int minFragSize = 3; // minimum fragment size
    private boolean doLinker = false;

    // output fragments
    private Map<String, Molecule> fragments = 
	new HashMap<String, Molecule>();
    // output linkers; only valid when type = FRAG_MURCKO
    private Map<String, Molecule> linkers = new HashMap<String, Molecule>();

    
    public MolecularFramework () {
	this (FragmentType.FRAG_MURCKO);
    }

    public MolecularFramework (FragmentType type) {
	this.type = type;
	try {
	    standardizer = new MolStandardizer ();
	    switch (type) {
	    case FRAG_RECAP:
		initRecap ();
		break;

	    case FRAG_MURCKO:
		initMurcko ();
		break;

	    case FRAG_CARBON:
		initCarbon ();
		break;
	    }
	}
	catch (Exception ex) { ex.printStackTrace(); }
    }

    protected void initRecap () throws Exception {
	RxnMolecule[] recapRxn = 
	    new RxnMolecule[RECAP_REACTIONS.length];

	MolHandler mh = new MolHandler ();
	for (int i = 0; i < RECAP_REACTIONS.length; ++i) {
	    mh.setMolecule(RECAP_REACTIONS[i]);
	    recapRxn[i] = (RxnMolecule)mh.getMolecule();
	}
		
	Molecule[] notlist = new Molecule[RECAP_NOTLIST.length];
	for (int i = 0; i < RECAP_NOTLIST.length; ++i) {
	    mh.setMolecule(RECAP_NOTLIST[i]);
	    notlist[i] = mh.getMolecule();
	}
		
	exfrag = new ExhaustiveFragmenter 
	    (recapRxn, null, new Recap (notlist, 2, 4, false));
	exfrag.setExtensive(false);
	exfrag.setMaxSetCount(8);
	exfrag.setMaxFragmentCount(4);
    }

    protected void initMurcko () {
	notList.add("c1ccccc1"); // ignore benzene... 
	notList.add("C1=CC=CC=C1");
    }

    protected void initCarbon () {
    }

    public void generateLinkers (boolean doLinker) {
	this.doLinker = doLinker;
    }
    public boolean generateLinkers () { return doLinker; }

    public void setMolecule (Molecule mol) {
	setMolecule (mol, true);
    }

    public void setMolecule (Molecule mol, boolean standardize) {
	if (standardize) {
	    try {
		this.mol =  mol.cloneMolecule();
		if (standardizer != null) {
		    if (!standardizer.standardize(this.mol)) {
			/*
			logger.log(Level.WARNING, "Can't standardize " 
				   + mol.getName() 
				   + "; no fragments generated!");
			// fails standardization, so don't bother...
			this.mol = null;
			*/
		    }
		}
	    }
	    catch (Exception ex) { 
		this.mol = null;
		logger.log(Level.WARNING, "Can't standardize " 
			   + mol.getName() 
			   + "; no fragments generated!", ex);
	    }
	}
	else {
	    try {
		MolHandler mh = new MolHandler (mol.toFormat("smiles:q"));
		this.mol = mh.getMolecule();
	    }
	    catch (Exception ex) {
		this.mol = mol.cloneMolecule();
		/*
		logger.log(Level.WARNING, "Unknown molecule format " 
			   + mol.getName() 
			   + "; no fragments generated!", ex);
		*/
	    }
	    this.mol.hydrogenize(false);
	    this.mol.expandSgroups();
	    this.mol.aromatize();
	    this.mol.dearomatize();
	}

	/*
	if (this.mol != null) {
	    this.mol.aromatize(Molecule.AROM_BASIC);
	}
	*/
	fragments.clear();
	linkers.clear();
    }

    public void addNotList (String fragment) {
	notList.add(fragment);
    }
    public void clearNotList () { 
	notList.clear();
    }

    public Molecule getMolecule () { return mol; }
    public int getHashCode () { return hash.getHashCode(mol); }
    public void setGenerateAtomMapping (boolean mapping) {
	this.generateAtomMapping = mapping;
    }
    public boolean getGenerateAtomMapping () { 
	return generateAtomMapping; 
    }
    public FragmentType getFragmentType () { return type; }
    public void setMaxNumRings (int max) {
	maxNumRings = max;
    }
    public int getMaxNumRings () { return maxNumRings; }
    public void setMinFragmentSize (int size) { minFragSize = size; }
    public int getMinFragmentSize () { return minFragSize; }
    public int getFragmentCount () { return fragments.size(); }
    public Enumeration<Molecule> getFragments () { 
	return Collections.enumeration(fragments.values());
    }
    public Enumeration<String> getFragmentAsSmiles () {
	return Collections.enumeration(fragments.keySet());
    }
    public Enumeration<Molecule> getLinkers () {
	return Collections.enumeration(linkers.values());
    }
    public Enumeration<String> getLinkerAsSmiles () {
	return Collections.enumeration(linkers.keySet());
    }
    public int getLinkerCount () { return linkers.size(); }
    
    public void run () {
	if (mol == null || mol.getAtomCount() < 3) {
	    return;
	}
	
	if (generateAtomMapping) {
	    MolAtom[] atoms = mol.getAtomArray();
	    for (int i = 0; i < atoms.length; ++i) {
		atoms[i].setAtomMap(i+1); // 1-based
	    }
	}
	
	switch (type) {
	case FRAG_CARBON:
	    for (MolBond b : mol.getBondArray()) {
		//b.setFlags(MolBond.TYPE_MASK, 1);
		b.setType(1);
		b.setFlags(MolBond.STEREO_MASK, 0);
		MolAtom a1 = b.getAtom1(), a2 = b.getAtom2();
		a1.setCharge(0);
		a2.setCharge(0);
		a1.setAtno(6);
		a2.setAtno(6);
	    }
	    // fall-thru
	    
	case FRAG_MURCKO:
	    generateFrameworkFragments (mol);
	    break;
	    
	case FRAG_RECAP:
	    generateRecapFragments (mol);
	    break;
	    
	default:
	    throw new IllegalArgumentException
		("Unknown fragment type: " + type + " specified!");
	}
    }

    protected void generateRecapFragments (final Molecule mol) {
	try {
	    Vector<Molecule> frags = new Vector<Molecule>();
	    exfrag.fragment(frags, mol);
	    int size = mol.getAtomCount();
	    for (Molecule m : frags) {
		if (m.getAtomCount() < size) {
		    fragments.put(m.toFormat("smiles:q0"), m);
		}
	    }
	}
	catch (chemaxon.sss.search.SearchException ex) {
	    ex.printStackTrace();
	}
    }

    protected void generateFrameworkFragments (final Molecule mol) {
	final int[][] sssr = mol.getSSSR();

	if (debug) {
	    MolAtom[] atoms = mol.getAtomArray();
	    for (int i = 0; i < atoms.length; ++i) {
		atoms[i].setAtomMap(i+1);
	    }
	    System.out.println("## " + MolStandardizer.canonicalSMILES(mol)
			       + " " + mol.getName());
	    for (int i = 0; i < atoms.length; ++i) {
		atoms[i].setAtomMap(0);
	    }

	    for (int j = 0; j < sssr.length; ++j) {
		int[] r = sssr[j];
		System.out.print(j+":");
		for (int i = 0; i < r.length; ++i) {
		    System.out.print(" " + (r[i]+1));
		}
		System.out.println();
	    }
	}

	if (sssr.length > maxNumRings) {
	    logger.log(Level.WARNING,"Number of SSSR ("+sssr.length
		       +") > max value (" + maxNumRings + "); "
		       + "truncating ring enumeration...");

	    // generate a fragment that contains all the rings
	    Vector<int[]> allrings = new Vector<int[]>();
	    for (int i = 0; i < sssr.length; ++i) {
		allrings.add(sssr[i]);
	    }
	    Molecule f = generateSubgraph (mol, allrings);
	    if (f != null && f.getAtomCount() >= minFragSize) {
		String frag = MolStandardizer.canonicalSMILES(f, false);
		if (!notList.contains(frag)) {
		    fragments.put(frag, f);
		}
	    }
	}

	GrayCode g = GrayCode.createBinaryGrayCode
	    (Math.min(sssr.length, maxNumRings));
	g.addObserver(new Observer () {
		public void update (Observable obs, Object arg) {
		    int[] g = (int[])arg;
		    Vector<int[]> comps = new Vector<int[]>();
		    for (int i = 0; i < g.length; ++i) {
			if (g[i] != 0) {
			    //int[] r = sssr[i];
				/*
				  for (int j = 0; j < r.length; ++j) {
				  System.out.print(" " + r[j]);
				  }
				  System.out.println();
				*/
			    comps.add(sssr[i]);
			}
		    }

		    if (!comps.isEmpty()) {
			Molecule f = generateSubgraph (mol, comps);
			if (f != null && f.getAtomCount() >= minFragSize) {
			    String frag = MolStandardizer.canonicalSMILES
				(f, false);
			    if (!notList.contains(frag)) {
				try {
				    MolImporter.importMol(frag, f);
				    f.dearomatize();
				}
				catch (Exception ex) {
				    logger.log(Level.SEVERE,
					       "Bogus fragment generated: " 
					       + frag, ex);
				}
				fragments.put(frag, f);
			    }
			}

			    
			if (debug) {
			    System.out.print("**");
			    for (int[] r : comps) {
				System.out.print(" {"+(r[0]+1));
				for (int i = 1; i < r.length; ++i) {
				    System.out.print(" "+(r[i]+1));
				}
				System.out.print("}");
			    }
			    System.out.println
				(" => " 
				 + (f != null 
				    ? MolStandardizer.canonicalSMILES(f,false)
				    : "null"));
			}
		    }
		}
	    });
	g.generate();

	if (type == FragmentType.FRAG_MURCKO && doLinker) {
	    generateLinkers (mol);
	}
    }

    protected BitSet[] getRingMemberships (Molecule mol) {
	return getRingMemberships (mol, mol.getSSSR());
    }

    protected BitSet[] getRingMemberships (Molecule mol, int[][] sssr) {
	BitSet[] rings = new BitSet[mol.getAtomCount()];
	for (int i = 0; i < sssr.length; ++i) {
	    for (int j = 0; j < sssr[i].length; ++j) {
		int a = sssr[i][j];
		BitSet bs = rings[a];
		if (bs == null) {
		    rings[a] = bs = new BitSet (sssr.length);
		}
		bs.set(i);
	    }
	}
	return rings;
    }

    protected Molecule generateSubgraph (Molecule mol, Vector<int[]> comps) {
	// generate subgraph corresponds to the given list of components
	Molecule m = mol.cloneMolecule();

	// make note of terminal =X atoms that aren't directly attached
	//  to rings
	int[] rsizes = m.getSmallestRingSizeForIdx();
	MolAtom[] atoms = m.getAtomArray();
	for (MolAtom a : atoms) {
	    a.setAtomMap(0);
	}
	for (int i = 0; i < atoms.length; ++i) {
	    MolAtom a = atoms[i];
	    if (a.isTerminalAtom() 
		&& a.getAtno() != 6 // just in case...
		&& a.getBondCount() > 0 // could be single atom molecule
		&& a.getBond(0).getType() == 2) {
		MolAtom xa = a.getBond(0).getOtherAtom(a);
		// save this attachment point
		int pos = m.indexOf(xa);
		if (rsizes[pos] == 0) { // only annotate non-ring 
		    xa.setAtomMap(pos+1);
		}
	    }
	}

	MolBond[] bonds = m.getBondArray();

	Set<Integer> keepAtoms = new HashSet<Integer>();
	for (int[] c : comps) {
	    for (int i = 0; i < c.length; ++i) {
		keepAtoms.add(c[i]);
	    }
	}

	// now remove all rings that are not in comps...
	Set<MolAtom> removeAtoms = new HashSet<MolAtom>();
	for (int i = 0; i < bonds.length; ++i) {
	    MolBond b = bonds[i];
	    if (m.isRingBond(i)) {
		int a1 = m.indexOf(b.getAtom1());
		int a2 = m.indexOf(b.getAtom2());
		boolean hasA1 = keepAtoms.contains(a1);
		boolean hasA2 = keepAtoms.contains(a2);
		if (hasA1 && hasA2) {
		}
		else if (hasA1) {
		    removeAtoms.add(b.getAtom2());
		}
		else if (hasA2) {
		    removeAtoms.add(b.getAtom1());
		}
		else {
		    removeAtoms.add(b.getAtom1());
		    removeAtoms.add(b.getAtom2());
		}
	    }
	}

	for (MolAtom a : removeAtoms) {
	    m.removeNode(a);
	}

	// now erode
	erode (m, false);

	// return the largest fragment...
	Molecule[] frags = m.convertToFrags();
	Molecule best = frags[0];
	for (int i = 1; i < frags.length; ++i) {
	    if (frags[i].getAtomCount() > best.getAtomCount()) {
		best = frags[i];
	    }
	}
	//best.aromatize(Molecule.AROM_BASIC);
	best.aromatize();
	best.dearomatize();

	// now check if we have bogus aromatic bonds; this can happen
	//  if we remove aromatic rings that are fused to non-aromatic
	//  rings.
	if (false) {
	    bonds = best.getBondArray();
	    for (int i = 0; i < bonds.length; ++i) {
		/*
		  if (bonds[i].getType() == MolBond.AROMATIC) {
		  best = null;
		  break;
		  }
		*/
		MolBond b = bonds[i];
		if (b.getType() == 2 || b.getType() == MolBond.AROMATIC) {
		    MolAtom a1 = b.getCTAtom1();
		    MolAtom a4 = b.getCTAtom4();
		    if (a1 != null && a4 != null) {
			b.setStereo2Flags(a1, a4, MolBond.TRANS|MolBond.CIS);
		    }
		}
	    }
	}


	if (best != null) {
	    // now reattach =X atoms (if any)
	    Vector<MolBond> newBonds = new Vector<MolBond>();
	    Vector<MolAtom> newAtoms = new Vector<MolAtom>();
	    for (MolAtom a : best.getAtomArray()) {
		int map = a.getAtomMap();
		if (map > 0) {
		    // there should be a terminal atom attached at 
		    //   this position
		    MolAtom x = mol.getAtom(map-1);
		    for (int i = 0; i < x.getBondCount(); ++i) {
			MolBond b = x.getBond(i);
			MolAtom xa = b.getOtherAtom(x);
			if (xa.isTerminalAtom()) {
			    // found it...
			    xa = new MolAtom (xa.getAtno());
			    MolBond bnd = new MolBond (xa, a);
			    bnd.setType(b.getType());
			    newAtoms.add(xa);
			    newBonds.add(bnd);
			}
		    }
		    a.setAtomMap(0);
		}
	    }

	    if (!newAtoms.isEmpty()) {
		// first add all new atoms
		for (MolAtom a : newAtoms) {
		    best.add(a);
		}
		// then the bonds
		for (MolBond b : newBonds) {
		    best.add(b);
		}
		best.valenceCheck();
	    }

	    try {
		best.setName(mol.getName());
		standardizer.standardize(best);
	    }
	    catch (Exception ex) {
		logger.log
		    (Level.WARNING, "Failed to standardize fragment", ex);
	    }
	}

	return best;
    }

    protected void generateLinkers (Molecule mol) {
	Molecule m = mol.cloneMolecule();
	MolBond[] bonds = m.getBondArray();

	Vector<MolBond> ringBonds = new Vector<MolBond>();
	int[] rings = m.getSmallestRingSizeForIdx();

	for (int i = 0; i < bonds.length; ++i) {
	    MolBond b = bonds[i];
	    if (m.isRingBond(i)) {
		ringBonds.add(b);
	    }
	    else {
		MolAtom a1 = b.getAtom1();
		MolAtom a2 = b.getAtom2();
		int ix1 = m.indexOf(a1), ix2 = m.indexOf(a2);

		if (rings[ix1] > 0 && rings[ix2] > 0) {
		    //ringBonds.add(b);
		    a1.setAtno(MolAtom.ANY);
		    a2.setAtno(MolAtom.ANY);
		}
		else if (rings[ix1] > 0) {
		    a1.setAtno(MolAtom.ANY);
		}
		else if (rings[ix2] > 0) {
		    a2.setAtno(MolAtom.ANY);
		}
	    }
	}

	for (MolBond b : ringBonds) {
	    m.removeEdge(b);
	}

	for (Molecule f : m.convertToFrags()) {
	    int nb = f.getBondCount();
	    if (nb > 0) {
		linkers.put(f.toFormat("smarts:q0"), f);
	    }
	}
    }

    protected static void erode (Molecule mol, boolean simple) {
	for (MolAtom atom; (atom = getNextTerminalAtom 
			    (mol, simple)) != null; ) {
	    mol.removeNode(atom);
	}
	mol.valenceCheck();
    }

    protected static MolAtom getNextTerminalAtom 
	(Molecule m, boolean simple) {
	int na = m.getAtomCount();
	int[] rsizes = m.getSmallestRingSizeForIdx();
	for (int i = 0; i < na; ++i) {
	    MolAtom a = m.getAtom(i);
	    if (a.isTerminalAtom()) {
		if (simple) {
		    return a;
		}
		else if (a.getBondCount() == 1) {
		    MolBond bond = a.getBond(0);
		    MolAtom xa = bond.getOtherAtom(a);
		    int xi = m.indexOf(xa);
		    if (//(a.getAtno() == 8 || a.getAtno() == 7) && 
			bond.getType() == 2 && rsizes[xi] > 0) {
			// keep double-bond O,N connecting to a 
			//   ring in tact
		    }
		    else if (xa.getAtno() == 16 && rsizes[xi] > 0) {
			// keep terminal attachments to S intach to preserve
			//  valence
		    }
		    /*
		      else if (rsizes[xi] > 0 && xa.getAtno() != 6
		      && a.getAtno() != 6) {
		      // terminal atom connecting to a non-carbon 
		      //  atom in a ring....
		      }
		    */
		    else {
			return a;
		    }
		}
	    }
	}
	return null;
    }

    public static MolecularFramework createMurckoInstance () {
	return new MolecularFramework (FragmentType.FRAG_MURCKO);
    }
    public static MolecularFramework createCarbonInstance () {
	return new MolecularFramework (FragmentType.FRAG_CARBON);
    }
    public static MolecularFramework createRECAPInstance () {
	return new MolecularFramework (FragmentType.FRAG_RECAP);
    }

    public static void main (String argv[]) throws Exception {
	if (argv.length < 2) {
	    System.out.println
		("Usage: MolecularFramework [murcko|carbon|recap] FILES...");
	    System.exit(1);
	}

	MolecularFramework.FragmentType type = 
	    MolecularFramework.FragmentType.FRAG_MURCKO;

	String which = argv[0];
	if (which.equalsIgnoreCase("carbon")) {
	    type = MolecularFramework.FragmentType.FRAG_CARBON;
	}
	else if (which.equalsIgnoreCase("recap")) {
	    type = MolecularFramework.FragmentType.FRAG_RECAP;
	}
	else {
	}

	MolecularFramework mf = new MolecularFramework (type);
	mf.setGenerateAtomMapping(false);

	MolStandardizer standardizer = new MolStandardizer ();
	
	for (int i = 1; i < argv.length; ++i) {
	    MolImporter molimp = new MolImporter (argv[i]);
	    try {
		for (Molecule mol = new Molecule (); molimp.read(mol); ) {
		    String name = mol.getName();
		    if (name == null || name.equals("")) {
			for (int j = 0; j < mol.getPropertyCount(); ++j) {
			    name = mol.getProperty(mol.getPropertyKey(j));
			    if (name != null && !name.equals("")) {
				break;
			    }
			}
			mol.setName(name);
		    }
		
		    mol.valenceCheck();
		    if (mol.hasValenceError()) {
			System.err.println
			    ("** warning: " + name + " has valence error");
		    }
		    
		    standardizer.standardize(mol);
		    Molecule[] frags = mol.convertToFrags();
		    for (Molecule frag : frags) {
			mf.setMolecule(frag, false);
			mf.run();
			
			for (Enumeration<Molecule> en = mf.getFragments();
			     en.hasMoreElements(); ) {
			    Molecule f = en.nextElement();
			    System.out.println
				(MolStandardizer.canonicalSMILES(f) + "\t" 
				 + name + "\t" + MolStandardizer.hashKey(f));
			}
		    }
			
		    /*
		      for (Enumeration<String> linker = mf.getLinkerAsSmiles();
		      linker.hasMoreElements(); ) {
		      String l = linker.nextElement();
		      System.out.println(l + "\t" + name);
		      }
		    */
		}
	    }
	    catch (Exception ex) {
		ex.printStackTrace();
	    }
	    molimp.close();
	}
    }
}
