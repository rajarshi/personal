package net.guha.util.cdk;

import net.guha.util.cdk.Base64;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.MDLWriter;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.layout.TemplateHandler;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.smiles.smarts.parser.TokenMgrError;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

import javax.vecmath.Vector2d;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Miscellaneous utility methods.
 *
 * @author Rajarshi Guha
 */
public class Misc {

    /**
     * Version of this library.
     */
    public static final String VERSION = "1.0";
    /**
     * Write multiple molecules to a single SD file.
     *
     * @param molecules  An array of molecules
     * @param filename   The filename to write to
     * @param writeProps If true, properties will be added as SD tags
     * @throws Exception if there is an error during writing
     */
    public static void writeMoleculesInOneFile(IAtomContainer[] molecules,
                                               String filename,
                                               int writeProps) throws Exception {
        MDLWriter writer = new MDLWriter(new FileWriter(new File(filename)));
        for (IAtomContainer molecule : molecules) {
            if (writeProps == 1) {
                Map propMap = molecule.getProperties();
                writer.setSdFields(propMap);
            }
            writer.write(molecule);
        }
    }

    /**
     * Write molecules to individual files.
     *
     * @param molecules  An array of molecules
     * @param prefix     The prefix for the individual files. By default this will be <i>mol</i>
     * @param writeProps If true properties will be written as SD tags
     * @throws Exception if there is an error during writing
     */
    public static void writeMolecules(IAtomContainer[] molecules, String prefix, int writeProps) throws Exception {
        int counter = 1;

        if (prefix == null || prefix.equals("")) prefix = "mol";

        for (IAtomContainer molecule : molecules) {
            String filename = prefix + counter + ".sdf";
            MDLWriter writer = new MDLWriter(new FileWriter(new File(filename)));
            if (writeProps == 1) {
                Map propMap = molecule.getProperties();
                writer.setSdFields(propMap);
            }
            writer.write(molecule);
            writer.close();
            counter += 1;
        }
    }

    /**
     * Loads one or more files into IAtomContainer objects.
     * <p/>
     * This method does not need knowledge of the format since it is
     * autodetected.
     * <p/>
     * <b>NOTE</B> It does not perform aromaticity detection
     *
     * @param filenames An array of String's containing the filenames of the
     *                  structures we want to load
     * @return An array of AtoContainer's
     * @throws org.openscience.cdk.exception.CDKException
     *          if there is an error when reading a file
     */
    public static IAtomContainer[] loadMolecules(String[] filenames) throws CDKException {
        Vector<IAtomContainer> v = new Vector<IAtomContainer>();
        DefaultChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
        try {
            int i;
            int j;

            for (i = 0; i < filenames.length; i++) {
                File input = new File(filenames[i]);
                ReaderFactory readerFactory = new ReaderFactory();
                ISimpleChemObjectReader reader = readerFactory.createReader(new FileReader(input));
                IChemFile content = (IChemFile) reader.read(new ChemFile());
                if (content == null) continue;

                List c = ChemFileManipulator.getAllAtomContainers(content);

                // we should do this loop in case we have files
                // that contain multiple molecules
                for (j = 0; j < c.size(); j++) v.add((IAtomContainer) c.get(j));
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new CDKException(e.toString());
        }

        // convert the vector to a simple array
        IAtomContainer[] retValues = new IAtomContainer[v.size()];
        for (int i = 0; i < v.size(); i++) {
            retValues[i] = v.get(i);
        }
        return retValues;
    }

    /**
     * Get a molecule with 2D coordinates.
     *
     * @param mol the input molecule
     * @return a new molecule with 2D coordinates
     * @throws Exception if there was an error generating coordinates
     */
    public static IMolecule get2DCoords(IAtomContainer mol) throws Exception {
        StructureDiagramGenerator sdg = new StructureDiagramGenerator();
        sdg.setTemplateHandler(new TemplateHandler(DefaultChemObjectBuilder.getInstance()));
        sdg.setMolecule((IMolecule) mol);
        sdg.generateCoordinates(new Vector2d(0, 1));
        return sdg.getMolecule();
    }

    /**
     * Get the substructure of a target structure based on a SMARTS pattern.
     *
     * @param target  The target molecule
     * @param pattern The SMARTS pattern for the substructure. The string can be a
     *                plain SMARTS or a Base64 encoded string
     * @return A fragment representing the fragmentif present, null otherwise
     * @throws org.openscience.cdk.exception.CDKException
     *          if there is an error during SMARTS
     *          parsing or matching
     */
    public static IAtomContainer getNeedle(IAtomContainer target, String pattern) throws CDKException {
        SMARTSQueryTool sqt = null;

        try {
            sqt = new SMARTSQueryTool(pattern);
        } catch (TokenMgrError e) {
            try {
                sqt = new SMARTSQueryTool(new String(Base64.decode(pattern)));
            } catch (CDKException e1) {
                throw new CDKException("Couldn't parse query");
            }
        }

        if (!sqt.matches(target)) return null;
        List<List<Integer>> matches = sqt.getUniqueMatchingAtoms();
        IAtomContainer ret = target.getBuilder().newAtomContainer();
        for (List<Integer> match : matches) {
            for (Integer idx : match) ret.addAtom(target.getAtom(idx));
        }

        // need to add the bonds, we only consider 2 atom bonds
        for (IBond bond : target.bonds()) {
            IAtom atom1 = bond.getAtom(0);
            IAtom atom2 = bond.getAtom(1);
            if (ret.contains(atom1) && ret.contains(atom2)) ret.addBond(bond);
        }

        return ret;
    }

}
