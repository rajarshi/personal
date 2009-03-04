package net.guha.apps;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.surface.NumericalSurface;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.PeriodicTable;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.util.List;

/**
 * Evaluate the ASA values described by Labute.
 * <p/>
 * This class evaluates the approximate surface area of a molecule, based
 * purely on connectivity. The method was described by Labute and the resultant
 * values can be used to derive the other VSA descriptors.
 * <p/>
 * Note that rather than take the VdW radii values and bond length values
 * listed in the original paper, we employ covalent radii (taken from the
 * Blue Obelisk data repository which corresponds to Bondi values) and derive
 * bond lengths by adding the covalent radii and apply the bond factor described in
 * the paper.
 * <p/>
 * The class does not attempt to add or remove hydrogens.
 */
public class VSA {

    private VSA() {
    }

    private double[] getVdwRadii(IAtomContainer atomContainer) {
        double[] vdwr = new double[atomContainer.getAtomCount()];
        for (int i = 0; i < atomContainer.getAtomCount(); i++) {
            IAtom atom = atomContainer.getAtom(i);
            String symbol = atom.getSymbol();
            if (symbol.equals("H")) {
                List<IAtom> connected = atomContainer.getConnectedAtomsList(atom);
                if (connected.get(0).getSymbol().equals("O")) vdwr[i] = 0.8;
                else if (connected.get(0).getSymbol().equals("N")) vdwr[i] = 0.7;
                else if (connected.get(0).getSymbol().equals("P")) vdwr[i] = 0.7;
                else vdwr[i] = 1.485;
            } else if (symbol.equals("C")) vdwr[i] = 1.95;
            else if (symbol.equals("N")) vdwr[i] = 1.95;
            else if (symbol.equals("F")) vdwr[i] = 1.496;
            else if (symbol.equals("P")) vdwr[i] = 2.287;
            else if (symbol.equals("S")) vdwr[i] = 2.185;
            else if (symbol.equals("Cl")) vdwr[i] = 2.044;
            else if (symbol.equals("Br")) vdwr[i] = 2.166;
            else if (symbol.equals("I")) vdwr[i] = 2.358;
            else if (symbol.equals("O")) {
                List<IAtom> connected = atomContainer.getConnectedAtomsList(atom);
                if (connected.size() == 1 &&
                        atomContainer.getBond(atom, connected.get(0)).getOrder().equals(IBond.Order.DOUBLE)) {
                    vdwr[i] = 1.810;
                } else if (connected.size() == 2) {
                    if (connected.get(0).getSymbol().equals("H")) {
                        List<IAtom> tmp = atomContainer.getConnectedAtomsList(connected.get(0));
                        for (IAtom tmp1 : tmp) {
                            if (!tmp1.equals(atom) && tmp1.getSymbol().equals("O") &&
                                    atomContainer.getBond(tmp1, connected.get(0)).getOrder().equals(IBond.Order.DOUBLE)) {
                                vdwr[i] = 2.152;
                                break;
                            }
                        }
                    } else if (connected.get(1).getSymbol().equals("H")) {
                        List<IAtom> tmp = atomContainer.getConnectedAtomsList(connected.get(1));
                        for (IAtom tmp1 : tmp) {
                            if (!tmp1.equals(atom) && tmp1.getSymbol().equals("O") &&
                                    atomContainer.getBond(tmp1, connected.get(1)).getOrder().equals(IBond.Order.DOUBLE)) {
                                vdwr[i] = 2.152;
                                break;
                            }
                        }
                    } else vdwr[i] = 1.779;
                }
            }
        }
        return vdwr;
    }

    /**
     * Get the ASA values for each atom in the molecule.
     * <p/>
     * The method does not add or remove hydrogens. Aromaticity should
     * be perceived before calling this method.
     *
     * @param atomContainer The molecule to consider.
     * @return An array of ASA values in the order of the atoms in the molecule
     */
    public static double[] getAtomVSA(IAtomContainer atomContainer) {
        double[] vi = new double[atomContainer.getAtomCount()];
        for (int i = 0; i < atomContainer.getAtomCount(); i++) {
            IAtom atom = atomContainer.getAtom(i);
            double radiusI = PeriodicTable.getCovalentRadius(atom.getSymbol());
            List<IAtom> connectedAtoms = atomContainer.getConnectedAtomsList(atom);
            double sum = 0.0;
            for (IAtom connectedAtom : connectedAtoms) {
                double radiusJ = PeriodicTable.getCovalentRadius(connectedAtom.getSymbol());
                double ideal = radiusI + radiusJ;

//                System.out.println(atom.getSymbol() + " " + connectedAtom.getSymbol() + " " + radiusI + " " + radiusJ + " " + ideal);
                IBond bond = atomContainer.getBond(atom, connectedAtom);
                if (bond.getFlag(CDKConstants.ISAROMATIC)) ideal = ideal - 0.1;
                else if (bond.getOrder().equals(IBond.Order.DOUBLE)) ideal -= 0.2;
                else if (bond.getOrder().equals(IBond.Order.TRIPLE)) ideal -= 0.3;

                double dIJ = Math.min(Math.max(Math.abs(radiusI - radiusJ), ideal), radiusI + radiusJ);
                sum += (radiusJ * radiusJ - (radiusI - dIJ) * (radiusI - dIJ)) / dIJ;
            }
            vi[i] = 4.0 * Math.PI * radiusI * radiusI - Math.PI * radiusI * sum;
        }
        return vi;
    }

    /**
     * Get the ASA for the whole molecule.
     * <p/>
     * The method does not add or remove hydrogens. Aromaticity should
     * be perceived before calling this method.
     *
     * @param atomContainer The molecule to consider.
     * @return The ASA of the molecule
     */
    public static double getVSA(IAtomContainer atomContainer) {
        double[] ret = getAtomVSA(atomContainer);
        double vsa = 0.0;
        for (double aRet : ret) vsa += aRet;
        return vsa;
    }

    public static void main(String[] args) throws CDKException {
        String[] smiles = {"C", "CC", "CBr", "CCC", "C(C)(C)(C)O", "c1ccccc1", "OC(=O)c1ccncc1C(=O)O"};
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

        for (String smile : smiles) {
            IAtomContainer mol = sp.parseSmiles(smile);
            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
            CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
            adder.addImplicitHydrogens(mol);
            AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol);
            CDKHueckelAromaticityDetector.detectAromaticity(mol);
            double vsa = getVSA(mol);
            System.out.println(smile + " vsa = " + vsa);
        }
    }

}
