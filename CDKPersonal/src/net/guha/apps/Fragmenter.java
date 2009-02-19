/**
 *
 */
package net.guha.apps;

import net.guha.util.cdk.MultiStructurePanel;
import net.guha.util.cdk.Renderer2DPanel;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.smiles.SmilesParser;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates fragments from a given molecule.
 * <p/>
 * Fragments are created by splitting the molecule at non-terminal and
 * non-ring bonds.
 * <p/>
 * The resultant fragments are then displayed in 2D form.
 * <p/>
 * Updated to recursively process the fragment list.
 *
 * @author Rajarshi Guha <rguha@indiana.edu>
 */
public class Fragmenter {

    /**
     * Split a molecule into fragments.
     * <p/>
     * The method considers bonds as splittable if they are
     * <ul>
     * <li>non-terminal
     * <li>Not in a ring
     * </ul>
     * In addition, the fragments returned will include rings if present.
     * <p/>
     * Make sure to remove hydrogens before calling this method!
     *
     * @param atomContainer The molecule to split
     * @return a list of fragments
     */
    public List<IAtomContainer> generateFragments(IAtomContainer atomContainer)
            throws CDKException {
        ArrayList<IAtomContainer> fragments = new ArrayList<IAtomContainer>();

        if (atomContainer.getBondCount() < 3)
            return fragments;

        // do ring detection
        AllRingsFinder allRingsFinder = new AllRingsFinder();
        IRingSet allRings;
        allRings = allRingsFinder.findAllRings(atomContainer);

        // find the splitable bonds
        ArrayList<IBond> splitableBonds = new ArrayList<IBond>();
        for (IBond bond : atomContainer.bonds()) {
            boolean isInRing = false;
            boolean isTerminal = false;

            // lets see if it's in a ring
            IRingSet rings = allRings.getRings(bond);
            if (rings.getAtomContainerCount() != 0)
                isInRing = true;

            // lets see if it is a terminal bond
//			Iterator bondAtoms = bond.atoms();
//			while (bondAtoms.hasNext()) {
//				IAtom atom = (IAtom) bondAtoms.next();
//				if (atomContainer.getConnectedAtomsCount(atom) == 1) {
//					isTerminal = true;
//					break;
//				}
//			}

            if (!isInRing && !isTerminal)
                splitableBonds.add(bond);
        }

        if (splitableBonds.size() == 0)
            return fragments;

        System.out.println("Found " + splitableBonds.size()
                + " bonds to split on");

        for (IBond bond : splitableBonds) {
            List<IAtomContainer> parts = splitMolecule(atomContainer, bond);

            if (fragments.size() == 0) {
                fragments.addAll(parts);
                continue;
            }

            // make sure we don't add the same fragment twice
            for (IAtomContainer partContainer : parts) {
                boolean present = false;
                for (IAtomContainer fragment : fragments) {
                    if (identicalAtoms(fragment, partContainer)) {
                        present = true;
                        break;
                    }
                }
                if (!present)
                    fragments.add(partContainer);
            }
        }

        // try and partition the fragments
        for (int i = 0; i < fragments.size(); i++) {
            IAtomContainer fragment = fragments.get(i);
            if (fragment.getBondCount() == 1)
                continue;
            List<IAtomContainer> frags = generateFragments(fragment);
            if (frags.size() == 0)
                continue;
            for (IAtomContainer partContainer : frags) {
                boolean present = false;
                for (IAtomContainer f : fragments) {
                    if (identicalAtoms(f, partContainer)) {
                        present = true;
                        break;
                    }
                }
                if (!present)
                    fragments.add(partContainer);
            }
        }

        return fragments;
    }

    private boolean identicalAtoms(IAtomContainer molecule1,
                                   IAtomContainer molecule2) {
        if (molecule1.getBondCount() != molecule2.getBondCount()
                && molecule1.getAtomCount() != molecule2.getAtomCount()) {
            return false;
        }

        int natom = molecule1.getAtomCount();
        int n = 0;
        for (int i = 0; i < molecule1.getAtomCount(); i++) {
            for (int j = 0; j < molecule2.getAtomCount(); j++) {
                if (molecule1.getAtom(i).equals(molecule2.getAtom(j))) {
                    n++;
                    break;
                }
            }
        }
        return n == molecule1.getAtomCount();
    }

    // the simplest approach would be to delete the specified bond and then
    // extract the two disconnected fragments. This implies that we have to
    // create an IAtomContainer object each time this method is called. To avoid
    // this we traverse the graph rather than do anything destructive
    private List<IAtomContainer> splitMolecule(IAtomContainer atomContainer,
                                               IBond bond) {
        List<IAtomContainer> ret = new ArrayList<IAtomContainer>();
        for (IAtom atom : bond.atoms()) {
            List<IBond> part = new ArrayList<IBond>();
            part.add(bond);
            part = traverse(atomContainer, atom, part);

            // at this point we have a partion which contains the bond we
            // split. This partition should actually 2 partitions:
            // - one with the splitting bond
            // - one without the splitting bond
            // note that this will lead to repeated fragments when we  do this
            // with adjacent bonds, so when we gather all the fragments we need
            // to check for repeats
            IAtomContainer partContainer;
            partContainer = makeAtomContainer(atom, part);

            // by checking for more than 2 atoms, we exclude single bond fragments
            // also if a fragment has the same number of atoms as the parent molecule,
            // it is the parent molecule, so we exclude it.
            if (partContainer.getAtomCount() > 2 && partContainer.getAtomCount() != atomContainer.getAtomCount())
                ret.add(partContainer);

            part.remove(0);
            partContainer = makeAtomContainer(atom, part);
            if (partContainer.getAtomCount() > 2 && partContainer.getAtomCount() != atomContainer.getAtomCount())
                ret.add(partContainer);
        }
        return ret;
    }

    private IAtomContainer makeAtomContainer(IAtom atom, List<IBond> parts) {
        IAtomContainer partContainer = DefaultChemObjectBuilder.getInstance()
                .newAtomContainer();
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
        List connectedBonds = atomContainer.getConnectedBondsList(atom);
        for (Object aBond : connectedBonds) {
            if (bondList.contains((IBond) aBond))
                continue;
            bondList.add((IBond) aBond);
            IAtom nextAtom = ((IBond) aBond).getConnectedAtom(atom);
            if (atomContainer.getConnectedAtomsCount(nextAtom) == 1)
                continue;
            traverse(atomContainer, nextAtom, bondList);
        }
        return bondList;
    }

    public IAtomContainer example1() throws CDKException {
        IAtomContainer mol = DefaultChemObjectBuilder.getInstance()
                .newAtomContainer();
        IAtom a1 = mol.getBuilder().newAtom("C");
        IAtom a2 = mol.getBuilder().newAtom("C");
        IAtom a3 = mol.getBuilder().newAtom("C");
        IAtom a4 = mol.getBuilder().newAtom("C");
        IAtom a5 = mol.getBuilder().newAtom("C");
        IAtom a6 = mol.getBuilder().newAtom("C");

        IBond b1 = mol.getBuilder().newBond(a1, a2);
        IBond b2 = mol.getBuilder().newBond(a2, a3);
        IBond b3 = mol.getBuilder().newBond(a3, a4);
        IBond b4 = mol.getBuilder().newBond(a3, a5);
        IBond b5 = mol.getBuilder().newBond(a5, a6);
        IBond b6 = mol.getBuilder().newBond(a1, a4);

        mol.addAtom(a1);
        mol.addAtom(a2);
        mol.addAtom(a3);
        mol.addAtom(a4);
        mol.addAtom(a5);
        mol.addAtom(a6);

        mol.addBond(b1);
        mol.addBond(b2);
        mol.addBond(b3);
        mol.addBond(b4);
        mol.addBond(b5);
        mol.addBond(b6);

        return mol;
    }

    public IAtomContainer example4() throws InvalidSmilesException {
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        return sp.parseSmiles("c1c(CC2CC2)cc(CNCC)cc1");
    }

    public static void main(String[] args) throws CDKException {
        IAtomContainer mol = null;

        Fragmenter fr = new Fragmenter();

        if (args.length == 0) {
            System.out
                    .println("Using c1c(CC2CC2)cc(CNCC)cc1\nYou can specify a SMILES string on the command line");
            mol = fr.example4();
        }
        if (args.length > 1) {
            System.out.println("Must supply a single SMILES string");
            System.exit(-1);
        } else if (args.length == 1) {
            SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
            mol = sp.parseSmiles(args[0]);
        }

        List<IAtomContainer> l = fr.generateFragments(mol);
        System.out.println("Got " + l.size() + " fragments");

        // make some nice pictures
        if (l.size() > 0) {
            int x = 200;
            int y = 200;
            Renderer2DPanel[] panels = new Renderer2DPanel[l.size() + 1];
            System.out.println("Creating image for specified molecule");
            panels[0] = new Renderer2DPanel(mol, x, y);
            panels[0].setBorder(BorderFactory.createEtchedBorder(
                    EtchedBorder.LOWERED, Color.red, Color.gray));

            int counter = 0;
            for (IAtomContainer atomContainer : l) {
                panels[counter + 1] = new Renderer2DPanel(atomContainer, x, y);
                panels[counter + 1].setBorder(BorderFactory
                        .createEtchedBorder(EtchedBorder.LOWERED));
                counter++;
            }

            JFrame frame = new MultiStructurePanel(panels, 3, 100, 100);
            frame.pack();
            frame.setVisible(true);
        }
    }


}