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
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class Recap {
    SMARTSQueryTool sqt;

    //patterns
    String[] patterns = {
            "c-c"
    };

    public Recap() throws CDKException {
        sqt = new SMARTSQueryTool("C");
    }

    public List<IAtomContainer> fragment(IAtomContainer atomContainer) throws CDKException {
        AllRingsFinder arf = new AllRingsFinder();
        arf.findAllRings(atomContainer);

        List<IAtomContainer> frags = new ArrayList<IAtomContainer>();
        frags.addAll(recapRule08(atomContainer));
        return frags;
    }

    private List<IAtomContainer> recapRule01(IAtomContainer atomContainer) throws CDKException {
        sqt.setSmarts("[NX3][$([CD3]=O)]");
        if (!sqt.matches(atomContainer)) return null;
        List<List<Integer>> matches = sqt.getUniqueMatchingAtoms();
        System.out.println("rule 1 : " + matches.size());
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

    private List<IAtomContainer> recapRule02(IAtomContainer atomContainer) throws CDKException {
        sqt.setSmarts("[OD2][$([CD3]=O)]");
        if (!sqt.matches(atomContainer)) return null;
        List<List<Integer>> matches = sqt.getUniqueMatchingAtoms();
        System.out.println("rule 2 : " + matches.size());
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

    private List<IAtomContainer> recapRule03(IAtomContainer atomContainer) throws CDKException {
        sqt.setSmarts("[ND3]-*");
        if (!sqt.matches(atomContainer)) return null;
        List<List<Integer>> matches = sqt.getUniqueMatchingAtoms();
        System.out.println("rule 3 : " + matches.size());
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


    private List<IAtomContainer> recapRule04(IAtomContainer atomContainer) throws CDKException {
        sqt.setSmarts("[ND3][$([CD3](=O))][ND3]");
        if (!sqt.matches(atomContainer)) return null;
        List<List<Integer>> matches = sqt.getUniqueMatchingAtoms();
        System.out.println("rule 4 : " + matches.size());
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

    private List<IAtomContainer> recapRule05(IAtomContainer atomContainer) throws CDKException {
        sqt.setSmarts("[OD2]-*");
        if (!sqt.matches(atomContainer)) return null;
        List<List<Integer>> matches = sqt.getUniqueMatchingAtoms();
        System.out.println("rule 5 : " + matches.size());
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

    private List<IAtomContainer> recapRule07(IAtomContainer atomContainer) throws CDKException {
        sqt.setSmarts("[ND4+]-*");
        if (!sqt.matches(atomContainer)) return null;
        List<List<Integer>> matches = sqt.getUniqueMatchingAtoms();
        System.out.println("rule 7 : " + matches.size());
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

    private List<IAtomContainer> recapRule06(IAtomContainer atomContainer) throws CDKException {
        sqt.setSmarts("C=C");
        if (!sqt.matches(atomContainer)) return null;
        List<List<Integer>> matches = sqt.getUniqueMatchingAtoms();
        System.out.println("rule 6 : " + matches.size());
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

    private List<IAtomContainer> recapRule08(IAtomContainer atomContainer) throws CDKException {
        sqt.setSmarts("n[CD4]");
        if (!sqt.matches(atomContainer)) return null;
        List<List<Integer>> matches = sqt.getUniqueMatchingAtoms();
        System.out.println("rule 6 : " + matches.size());
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

    // TODO find out why it doesn't match
    private List<IAtomContainer> recapRule09(IAtomContainer atomContainer) throws CDKException {
        sqt.setSmarts("[R0]-[$([NRD3][CR]=O)]");
        if (!sqt.matches(atomContainer)) return null;
        List<List<Integer>> matches = sqt.getUniqueMatchingAtoms();
        System.out.println("rule 9 : " + matches.size());
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


    private List<IAtomContainer> recapRule10(IAtomContainer atomContainer) throws CDKException {
        sqt.setSmarts("c-c");
        if (!sqt.matches(atomContainer)) return null;
        List<List<Integer>> matches = sqt.getUniqueMatchingAtoms();
        System.out.println("rule 10 : " + matches.size());
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

    private List<IAtomContainer> recapRule11(IAtomContainer atomContainer) throws CDKException {
        sqt.setSmarts("[ND3][$(S(=O)(=O)*)]");
        if (!sqt.matches(atomContainer)) return null;
        List<List<Integer>> matches = sqt.getUniqueMatchingAtoms();
        System.out.println("rule 11 : " + matches.size());
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

    private IAtomContainer getNeedle(IAtomContainer atomContainer, IBond bond) {
        IAtomContainer ret = atomContainer.getBuilder().newAtomContainer();
        ret.addBond(bond);
        return ret;
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
        String smiles = "n1cn(C(C)(C)C)cc1";
        IMolecule mol = sp.parseSmiles(smiles);
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);

        List<IAtomContainer> f = recap.fragment(mol);
        System.out.println("f.size() = " + f.size());

        Renderer2DPanel[] panels = new Renderer2DPanel[f.size() + 1];
        panels[0] = new Renderer2DPanel(Misc.get2DCoords(mol), 200, 200);
        panels[0].setBorder(BorderFactory.createEtchedBorder(
                EtchedBorder.LOWERED, Color.red, Color.gray));

        for (int i = 0; i < f.size(); i++)
            panels[i + 1] = new Renderer2DPanel(Misc.get2DCoords(f.get(i)), 200, 200);
        MultiStructurePanel msp = new MultiStructurePanel(panels, 4, 200, 200);
        msp.setVisible(true);

    }
}
