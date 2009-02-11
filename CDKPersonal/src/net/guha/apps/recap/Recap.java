package net.guha.apps.recap;

import net.guha.util.cdk.Misc;
import net.guha.util.cdk.MultiStructurePanel;
import net.guha.util.cdk.Renderer2DPanel;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.util.*;


public class Recap {
    private boolean verbose = false;

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
            "[R0]-[$([NRD3][CR]=O)]", // rule 9
            "c-c", // rule 10
            "[ND3][$(S(=O)(=O)*)]", // rule 11
    };

    public List<IAtomContainer> fragment(IAtomContainer atomContainer) throws Exception {

        sqt = new SMARTSQueryTool("C");
        AllRingsFinder arf = new AllRingsFinder();
        arf.findAllRings(atomContainer);

        List<IAtomContainer> frags = dofrag(atomContainer);
        List<IAtomContainer> newFrags = new ArrayList<IAtomContainer>();
        List<IAtomContainer> fragsToDelete = new ArrayList<IAtomContainer>();
        while (true) {
            for (IAtomContainer frag : frags) {
                List<IAtomContainer> tmp = dofrag(frag);
                if (tmp.size() > 0) {
                    newFrags.addAll(tmp);
                    fragsToDelete.add(frag);
                }
            }
            if (fragsToDelete.size() > 0) {
                for (IAtomContainer frag : fragsToDelete) {
                    frags.remove(frag);
                }
                frags.addAll(newFrags);
                newFrags.clear();
                fragsToDelete.clear();
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
            IAtomContainer[] parts = splitMolecule(atomContainer, splitBond);
            if (parts[0].getAtomCount() == 1 || parts[1].getAtomCount() == 1) return null;
            ret.add(parts[0]);
            ret.add(parts[1]);
        }
        return ret;
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


    // TODO find out why it doesn't match
    private List<IAtomContainer> recapRule09(String pattern, IAtomContainer atomContainer) throws CDKException {
        sqt.setSmarts("[R0]-[$([NRD3][CR]=O)]");
        if (!sqt.matches(atomContainer)) return null;
        List<List<Integer>> matches = sqt.getUniqueMatchingAtoms();
        if (verbose) System.out.println("rule " + pattern + ": " + matches.size());
        List<IAtomContainer> ret = new ArrayList<IAtomContainer>();
        for (List<Integer> path : matches) {
            IAtom left = atomContainer.getAtom(path.get(0));
            IAtom right = atomContainer.getAtom(path.get(1));
            IBond splitBond = atomContainer.getBond(left, right);
            if (splitBond.getFlag(CDKConstants.ISINRING)) continue;
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
                                           IBond bond) {
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

    private IAtomContainer makeAtomContainer(IAtom atom, List<IBond> parts) {
        IAtomContainer partContainer = DefaultChemObjectBuilder.getInstance().newAtomContainer();
        partContainer.addAtom(atom);
        for (IBond aBond : parts) {
            for (IAtom bondedAtom : aBond.atoms()) {
                if (!partContainer.contains(bondedAtom))
                    partContainer.addAtom(bondedAtom);
            }
            partContainer.addBond(aBond);
        }
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

    private String[] getUniqueFragments(List<IAtomContainer> frags) {
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

    public static void main(String[] args) throws Exception {
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
        IMolecule mol = sp.parseSmiles(smiles);
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);

        List<IAtomContainer> f = recap.fragment(mol);
        System.out.println("f.size() = " + f.size());
        recap.displayFrags(f);
        String[] cansmi = recap.getUniqueFragments(f);
        for (String s : cansmi) System.out.println(s);


    }
}
