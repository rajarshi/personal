package net.guha.util.cdk;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.io.IChemObjectReader;
import org.openscience.cdk.io.MDLWriter;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;
import java.util.Vector;
import java.util.List;

/**
 * Miscellaneous utility methods.
 *
 * @author Rajarshi Guha
 */
public class Misc {

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
     *
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
                IChemObjectReader reader = readerFactory.createReader(new FileReader(input));
                IChemFile content = (IChemFile) reader.read(builder.newChemFile());
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



}
