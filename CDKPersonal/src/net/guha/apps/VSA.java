package net.guha.apps;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.qsar.descriptors.molecular.BCUTDescriptor;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluate the ASA values described by Labute {@cdk.cite LABUTE2000}.
 * <p/>
 * This class evaluates the approximate surface area of a molecule, based
 * purely on connectivity. The method was described by Labute {@cdk.cite LABUTE2000}
 * and the resultant
 * values can be used to derive the other VSA descriptors.
 * <p/>
 * The atomic Van der Waals radii and ideal bond lengths are taken from the paper.
 * <p/>
 * The class does not attempt to add or remove hydrogens, but expects explicit H's to be
 * present.
 *
 * @cdk.author Rajarshi Guha
 * @cdk.created 2009-03-05
 * @cdk.keyword ASA
 * @cdk.keyword surface area
 * @cdk.keyword smiles
 * @cdk.module extra
 *
 */
public class VSA {
    private Map<String, Double> bondLengths = new HashMap<String, Double>();

    public VSA() {
        bondLengths.put("BrBr", 2.540);
        bondLengths.put("FF", 1.280);
        bondLengths.put("BrC", 1.970);
        bondLengths.put("FH", 0.870);
        bondLengths.put("BrCl", 2.360);
        bondLengths.put("FI", 2.040);
        bondLengths.put("BrF", 1.850);
        bondLengths.put("FN", 1.410);
        bondLengths.put("BrH", 1.440);
        bondLengths.put("FO", 1.320);
        bondLengths.put("BrI", 2.650);
        bondLengths.put("FP", 1.500);
        bondLengths.put("BrN", 1.840);
        bondLengths.put("FS", 1.640);
        bondLengths.put("BrO", 1.580);
        bondLengths.put("HI", 1.630);
        bondLengths.put("BrP", 2.370);
        bondLengths.put("HN", 1.010);
        bondLengths.put("BrS", 2.210);
        bondLengths.put("HO", 0.970);
        bondLengths.put("CC", 1.540);
        bondLengths.put("HP", 1.410);
        bondLengths.put("CCl", 1.800);
        bondLengths.put("HS", 1.310);
        bondLengths.put("CF", 1.350);
        bondLengths.put("II", 2.920);
        bondLengths.put("CH", 1.060);
        bondLengths.put("IN", 2.260);
        bondLengths.put("CI", 2.120);
        bondLengths.put("IO", 2.140);
        bondLengths.put("CN", 1.470);
        bondLengths.put("IP", 2.490);
        bondLengths.put("CO", 1.430);
        bondLengths.put("IS", 2.690);
        bondLengths.put("CP", 1.850);
        bondLengths.put("NN", 1.450);
        bondLengths.put("CS", 1.810);
        bondLengths.put("NO", 1.460);
        bondLengths.put("ClCl", 2.310);
        bondLengths.put("NP", 1.60);
        bondLengths.put("ClF", 1.630);
        bondLengths.put("NS", 1.760);
        bondLengths.put("C1H", 1.220);
        bondLengths.put("OO", 1.470);
        bondLengths.put("ClI", 2.560);
        bondLengths.put("OP", 1.570);
        bondLengths.put("ClN", 1.740);
        bondLengths.put("OS", 1.570);
        bondLengths.put("ClO", 1.410);
        bondLengths.put("PP", 2.260);
        bondLengths.put("ClP", 2.010);
        bondLengths.put("PS", 2.070);
        bondLengths.put("ClS", 2.070);
        bondLengths.put("SS", 2.050);
    }


    private double getVdwRadii(IAtomContainer atomContainer, IAtom atom) {
        double vdwr = 0.0;
        String symbol = atom.getSymbol();
        if (symbol.equals("H")) {
            List<IAtom> connected = atomContainer.getConnectedAtomsList(atom);
            if (connected.get(0).getSymbol().equals("O")) vdwr = 0.8;
            else if (connected.get(0).getSymbol().equals("N")) vdwr = 0.7;
            else if (connected.get(0).getSymbol().equals("P")) vdwr = 0.7;
            else vdwr = 1.485;
        } else if (symbol.equals("C")) vdwr = 1.95;
        else if (symbol.equals("N")) vdwr = 1.95;
        else if (symbol.equals("F")) vdwr = 1.496;
        else if (symbol.equals("P")) vdwr = 2.287;
        else if (symbol.equals("S")) vdwr = 2.185;
        else if (symbol.equals("Cl")) vdwr = 2.044;
        else if (symbol.equals("Br")) vdwr = 2.166;
        else if (symbol.equals("I")) vdwr = 2.358;
        else if (symbol.equals("O")) {
            List<IAtom> connected = atomContainer.getConnectedAtomsList(atom);
            if (connected.size() == 1 &&
                    atomContainer.getBond(atom, connected.get(0)).getOrder().equals(IBond.Order.DOUBLE)) {
                vdwr = 1.810;
            } else if (connected.size() == 2) {
                if (connected.get(0).getSymbol().equals("H")) {
                    List<IAtom> tmp = atomContainer.getConnectedAtomsList(connected.get(0));
                    for (IAtom tmp1 : tmp) {
                        if (!tmp1.equals(atom) && tmp1.getSymbol().equals("O") &&
                                atomContainer.getBond(tmp1, connected.get(0)).getOrder().equals(IBond.Order.DOUBLE)) {
                            vdwr = 2.152;
                            break;
                        }
                    }
                } else if (connected.get(1).getSymbol().equals("H")) {
                    List<IAtom> tmp = atomContainer.getConnectedAtomsList(connected.get(1));
                    for (IAtom tmp1 : tmp) {
                        if (!tmp1.equals(atom) && tmp1.getSymbol().equals("O") &&
                                atomContainer.getBond(tmp1, connected.get(1)).getOrder().equals(IBond.Order.DOUBLE)) {
                            vdwr = 2.152;
                            break;
                        }
                    }
                } else vdwr = 1.779;
            }
        }

        return vdwr;
    }

    /**
     * Get the ASA values for each atom in the molecule.
     * <p/>
     * The method does not add or remove hydrogens, but expects that
     * explicit H's are present. Aromaticity should
     * be perceived before calling this method.
     *
     * @param atomContainer The molecule to consider.
     * @return An array of ASA values in the order of the atoms in the molecule
     * @throws org.openscience.cdk.exception.CDKException
     *          if a bond not listed in the original
     *          Labute paper was encountered
     */
    public double[] getAtomVSA(IAtomContainer atomContainer) throws CDKException {
        double[] vi = new double[atomContainer.getAtomCount()];
        for (int i = 0; i < atomContainer.getAtomCount(); i++) {
            IAtom atom = atomContainer.getAtom(i);
            double radiusI = getVdwRadii(atomContainer, atom);
            List<IAtom> connectedAtoms = atomContainer.getConnectedAtomsList(atom);
            double sum = 0.0;
            for (IAtom connectedAtom : connectedAtoms) {
                double radiusJ = getVdwRadii(atomContainer, connectedAtom);

                String key = "";
                if (atom.getSymbol().compareTo(connectedAtom.getSymbol()) < 0)
                    key = atom.getSymbol() + connectedAtom.getSymbol();
                else
                    key = connectedAtom.getSymbol() + atom.getSymbol();

                Double ideal = bondLengths.get(key);
                if (ideal == null)
                    throw new CDKException("Don't know about this bond type: " + atom.getSymbol() + " " + connectedAtom.getSymbol());

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
     * The method does not add or remove hydrogens, but expects explicit H's.
     * Aromaticity should be perceived before calling this method.
     *
     * @param atomContainer The molecule to consider.
     * @return The ASA of the molecule
     * @throws org.openscience.cdk.exception.CDKException
     *          if a bond not listed in the original
     *          Labute paper was encountered
     */
    public double getVSA(IAtomContainer atomContainer) throws CDKException {
        double[] ret = getAtomVSA(atomContainer);
        double vsa = 0.0;
        for (double aRet : ret) vsa += aRet;
        return vsa;
    }

    public static void main(String[] args) throws CDKException, IOException {
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        String filename = "/home/rguha/vsa.csv";
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        VSA vsa = new VSA();
        while (true) {
            String line = reader.readLine();
            if (line == null) break;
            String[] toks = line.split(",");
            double vsa_obs = Double.parseDouble(toks[0]);
            String smiles = toks[1];

            IAtomContainer mol = sp.parseSmiles(smiles);
            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
            CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
            adder.addImplicitHydrogens(mol);
            AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol);
            CDKHueckelAromaticityDetector.detectAromaticity(mol);
            double vsa_calc = vsa.getVSA(mol);
            System.out.println(smiles + "  " + vsa_calc + " " + vsa_obs + " " + Math.abs(vsa_calc - vsa_obs));
        }


    }

}
