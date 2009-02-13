package net.guha.apps.recap;

import net.guha.util.cdk.Misc;
import net.guha.util.cdk.MultiStructurePanel;
import net.guha.util.cdk.Renderer2DPanel;
import org.openscience.cdk.Bond;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Recap {
    private boolean verbose = true;

    SMARTSQueryTool sqt;
    private int minFragSize = 3;
    private String[] patterns = {
            "[NX3][$([CD3]=O)]", // rule 1
            "[OD2][$([CD3]=O)]", //rule 2
            "[ND3]-*", // rule 3
            "[ND3][$([CD3](=O))][ND3]", //rule 4
            "[OD2]-*", // rule 5
            "C=C", // rule 6
            "[ND4+]-*", // rule 7
            "n[CD4]", // rule 8

            //   [!R]-[$([NRD3][CR]=O)] seems to work but [R0]-[$([NRD3][CR]=O)] does not
            "[!R]-[$([NRD3][CR]=O)]", // rule 9

            // broken :(
            "c-c", // rule 10
            "[ND3][$(S(=O)(=O)*)]", // rule 11
    };

    public Recap() throws CDKException {
        sqt = new SMARTSQueryTool("C");
    }

    public List<IAtomContainer> fragment(IAtomContainer atomContainer) throws Exception {

        AllRingsFinder arf = new AllRingsFinder();
        arf.findAllRings(atomContainer);

        List<IAtomContainer> frags = dofrag(atomContainer);
        List<IAtomContainer> newFrags = new ArrayList<IAtomContainer>();
        List<IAtomContainer> fragsToDelete = new ArrayList<IAtomContainer>();

        if (verbose) System.out.println("Start with frags "+frags.size());
        while (true) {
            for (IAtomContainer frag : frags) {
                List<IAtomContainer> tmp = dofrag(frag);
                if (tmp.size() > 0) {
                    newFrags.addAll(tmp);
                    fragsToDelete.add(frag);
                }
            }
            if (fragsToDelete.size() > 0) {
                if (verbose) System.out.println("Will delete frags "+fragsToDelete.size());
                for (IAtomContainer frag : fragsToDelete) {
                    frags.remove(frag);
                }
                frags.addAll(newFrags);
                newFrags.clear();
                fragsToDelete.clear();
                if (verbose) System.out.println("Starting again with frags "+frags.size());
            } else break;
        }
        return frags;
    }

    private List<IAtomContainer> dofrag(IAtomContainer atomContainer) throws CDKException {
        List<IAtomContainer> frags = new ArrayList<IAtomContainer>();
        for (int i = 1; i < patterns.length + 1; i++) {
            List<IAtomContainer> tmp = null;
            if (i == 4) {
                tmp = recapRule04(patterns[i - 1], atomContainer);
            } else if (i == 10) {
                continue;
            } else {
                tmp = recapRule2Atom(patterns[i - 1], atomContainer);
            }
            if (tmp != null) {
                for (IAtomContainer frag : tmp) {
                    if (frag.getAtomCount() >= minFragSize) frags.add(frag);
                }
            }
        }
        return frags;
    }

    // this method handles patterns where we are supposed to split
    // at a single bond and so can be defined by a 2 atom SMARTS
    private List<IAtomContainer> recapRule2Atom(String pattern, IAtomContainer atomContainer) throws CDKException {
        sqt.setSmarts(pattern);
        if (!sqt.matches(atomContainer)) return null;
        List<List<Integer>> matches = sqt.getUniqueMatchingAtoms();
        if (verbose) System.out.println("rule " + pattern + " : " + matches.size());
        List<IAtomContainer> ret = new ArrayList<IAtomContainer>();
        for (List<Integer> path : matches) {
            IAtom left = atomContainer.getAtom(path.get(0));
            IAtom right = atomContainer.getAtom(path.get(1));
            IBond splitBond = atomContainer.getBond(left, right);
            if (splitBond.getFlag(CDKConstants.ISINRING)) continue;

            // TODO is this correct?
            if (isTerminal(atomContainer, splitBond)) continue;
            
            IAtomContainer[] parts = splitMolecule(atomContainer, splitBond);            
            ret.add(parts[0]);
            ret.add(parts[1]);
        }
        return ret;
    }

    private boolean isTerminal(IAtomContainer atomContainer, IBond bond) {
        for (IAtom atom : bond.atoms()) {
            if (atomContainer.getConnectedAtomsCount(atom) == 1) return true;
        }
        return false;
    }

    private List<IAtomContainer> recapRule04(String pattern, IAtomContainer atomContainer) throws CDKException {
        sqt.setSmarts(pattern);
        if (!sqt.matches(atomContainer)) return null;
        List<List<Integer>> matches = sqt.getUniqueMatchingAtoms();
        if (verbose) System.out.println("rule " + pattern + " : " + matches.size());
        List<IAtomContainer> ret = new ArrayList<IAtomContainer>();
        for (List<Integer> path : matches) {
            IAtom c = null;
            for (Integer atomidx : path) {
                if (atomContainer.getAtom(atomidx).getSymbol().equals("C"))
                    c = atomContainer.getAtom(atomidx);
            }
            System.out.println("c = " + c);
            List<IBond> connectedBonds = atomContainer.getConnectedBondsList(c);
            for (IBond bond : connectedBonds) {
                if (!bond.getOrder().equals(IBond.Order.DOUBLE) &&
                        !bond.getFlag(CDKConstants.ISINRING)) {
                    IAtomContainer[] parts = splitMolecule(atomContainer, bond);
                    ret.add(parts[0]);
                    ret.add(parts[1]);
                }
            }
        }
        return ret;
    }


    //[R0]-[$([NRD3][CR]=O)]
    // [R0][ND3R][CR]=O
    private List<IAtomContainer> recapRule09(String pattern, IAtomContainer atomContainer) throws CDKException {
        sqt.setSmarts(pattern);
        if (!sqt.matches(atomContainer)) return null;
        List<List<Integer>> matches = sqt.getUniqueMatchingAtoms();
        if (verbose) System.out.println("rule " + pattern + ": " + matches.size());
        List<IAtomContainer> ret = new ArrayList<IAtomContainer>();
        for (List<Integer> path : matches) {
            IAtom n = null;
            for (Integer idx : path) {
                IAtom atom = atomContainer.getAtom(idx);
                if (atom.getSymbol().equals("N")) {
                    n = atom;
                    break;
                }
            }
            IBond splitBond = null;
            for (IBond bond : atomContainer.getConnectedBondsList(n)) {
                if (!bond.getFlag(CDKConstants.ISINRING) && !bond.getOrder().equals(IBond.Order.DOUBLE)) {
                    splitBond = bond;
                    break;
                }
            }
            IAtomContainer[] parts = splitMolecule(atomContainer, splitBond);
            ret.add(parts[0]);
            ret.add(parts[1]);
        }
        return ret;
    }

    /**
     * Split a molecule into two parts at the specified bond.
     *
     * @param atomContainer The molecule to split
     * @param bond          The bond to split at
     * @return A 2-element array of IAtomContainer's representing the fragments
     */
    private IAtomContainer[] splitMolecule(IAtomContainer atomContainer,
                                           IBond bond) throws CDKException {
        IAtomContainer[] ret = new IAtomContainer[2];


        IAtom left = bond.getAtom(0);
        IAtom right = bond.getAtom(1);

        List<IBond> part = new ArrayList<IBond>();
        part.add(bond);
        part = traverse(atomContainer, left, part);
        part.remove(bond);
        ret[0] = makeAtomContainer(left, part);

        part = new ArrayList<IBond>();
        part.add(bond);
        part = traverse(atomContainer, right, part);
        part.remove(bond);
        ret[1] = makeAtomContainer(right, part);
        return ret;
    }

    private IAtomContainer makeAtomContainer(IAtom atom, List<IBond> parts) throws CDKException {
        IAtomContainer partContainer = DefaultChemObjectBuilder.getInstance().newAtomContainer();
        partContainer.addAtom(atom);

        IPseudoAtom pseudoAtom = atom.getBuilder().newPseudoAtom("*");
        partContainer.addAtom(pseudoAtom);
        partContainer.addBond(new Bond(atom, pseudoAtom));

        for (IBond aBond : parts) {
            for (IAtom bondedAtom : aBond.atoms()) {
                if (!partContainer.contains(bondedAtom))
                    partContainer.addAtom(bondedAtom);
            }
            partContainer.addBond(aBond);
        }
      
        CDKHueckelAromaticityDetector.detectAromaticity(partContainer);
        return partContainer;
    }

    private List<IBond> traverse(IAtomContainer atomContainer, IAtom atom,
                                 List<IBond> bondList) {
        List<IBond> connectedBonds = atomContainer.getConnectedBondsList(atom);
        for (IBond aBond : connectedBonds) {
            if (bondList.contains(aBond))
                continue;
            bondList.add(aBond);
            IAtom nextAtom = aBond.getConnectedAtom(atom);
            if (atomContainer.getConnectedAtomsCount(nextAtom) == 1)
                continue;
            traverse(atomContainer, nextAtom, bondList);
        }
        return bondList;
    }

    protected String[] getUniqueFragmentsAasSmiles(List<IAtomContainer> frags) {
        SmilesGenerator sg = new SmilesGenerator();
        List<String> cansmi = new ArrayList<String>();
        for (IAtomContainer frag : frags) cansmi.add(sg.createSMILES(frag.getBuilder().newMolecule(frag)));
        Set<String> uniqsmi = new HashSet<String>(cansmi);
        return uniqsmi.toArray(new String[]{});
    }

    public void displayFrags(List<IAtomContainer> frags) throws Exception {
        Renderer2DPanel[] panels = new Renderer2DPanel[frags.size()];
        for (int i = 0; i < frags.size(); i++)
            panels[i] = new Renderer2DPanel(Misc.get2DCoords(frags.get(i)), 300, 300);
        MultiStructurePanel msp = new MultiStructurePanel(panels, 4, 300, 300);
        msp.setTitle(frags.size() + " fragments");
        msp.setVisible(true);
    }

    public static void main(String[] args)  throws Exception {
        RecapUI ui = new RecapUI();
        ui.setVisible(true);
        Recap recap = new Recap();

        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
//        String smiles = "c1ccccc1c1ccccc1";
//        String smiles = "N(C)(C)C(=O)CCCC(=O)N(N)C";
//        String smiles = "CCN(C)CN";
//        String smiles = "CNSCNN";
//        String smiles = "CC(=O)OC";
//        String smiles = "CC=CCC=CN";
//        String smiles = "N(C)(C)CCCC";
//        String smiles = "N1(CC)C(=O)CCCC1";
//        String smiles = "N(CCC)(C)S(=O)(=O)CC(=O)CC";
//        String smiles = "CNOCN";
//        String smiles = "N(Cl)(I)C(=O)N(F)(Br)";
//        String smiles = "[N+](C)(C)(CCC)(C)";

//        String smiles = "c1cccn1C(C)(C)CC";
//        String smiles = "n1cn(C(C)(C)C)cc1";

        String smiles = "COC1CN(CCC1NC(=O)C2=CC(=C(C=C2OC)N)Cl)CCCOC3=CC=C(C=C3)F";
//        String smiles = "Fc1ccccc1OC";
        IMolecule mol = sp.parseSmiles(smiles);
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);

//        List<IAtomContainer> s1 = recap.recapRule09("[R0][ND3R][CR]=O", mol);
//        List<IAtomContainer> s1 = recap.recapRule2Atom("[OD2]-*", mol);
//        if (s1 != null) {
//            s1.add(0, mol);
//            recap.displayFrags(s1);
//        } else System.out.println("no fragments");
//
//        List<IAtomContainer> f = recap.fragment(mol);
//        System.out.println("f.size() = " + f.size());
//        recap.displayFrags(f);
//        String[] cansmi = recap.getUniqueFragments(f);
//        for (String s : cansmi) System.out.println(s);


    }
}
