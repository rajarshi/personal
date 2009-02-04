

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.*;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSMILESReader;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.io.*;
import java.util.BitSet;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: rguha
 * Date: Oct 13, 2008
 * Time: 12:16:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class sims {
    public static void genfp(String ifilename) throws IOException, CDKException {
        String prefix = ifilename.split("\\.")[0];

        HashMap<String, IFingerprinter> hash = new HashMap<String, IFingerprinter>();
        hash.put(prefix + "-cdk-s1024.txt", new Fingerprinter());
        hash.put(prefix + "-cdk-e1024.txt", new ExtendedFingerprinter());
        hash.put(prefix + "-cdk-maccs.txt", new MACCSFingerprinter());
        hash.put(prefix + "-cdk-estate.txt", new EStateFingerprinter());

        for (String s : hash.keySet()) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(s)));
            writer.write("HeaderLine\n");
            IFingerprinter fp = hash.get(s);
            IteratingSMILESReader reader = new IteratingSMILESReader(new FileReader(new File(ifilename)), DefaultChemObjectBuilder.getInstance());

            System.out.println("Generating "+s);
            System.out.flush();
            
            int n = 0;
            while (reader.hasNext()) {
                IAtomContainer molecule = (IAtomContainer) reader.next();
                try {
                    AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
                } catch (CDKException e) {
                    throw new CDKException("Error in atom typing" + e.toString());
                }

                // do a aromaticity check
                try {
                    CDKHueckelAromaticityDetector.detectAromaticity(molecule);
                } catch (CDKException e) {
                    throw new CDKException("Error in aromaticity detection");
                }

                BitSet bits = fp.getFingerprint(molecule);
                String title = (String) molecule.getProperty(CDKConstants.TITLE);

                writer.write(title + " " + bits.toString() + "\n");

                n += 1;
                if (n % 100 == 0) {
                    System.out.print("\rProcessed "+n);
                    System.out.flush();
                }
            }
            writer.close();
            reader.close();
        }
    }

    public static void main(String[] args) throws IOException, CDKException {
        if (args.length != 2) {
            System.out.println("Usage: sims actives.smi decoys.smi");
            System.exit(0);
        }

        String actives = args[0];
        String decoys = args[1];

        genfp(actives);
        genfp(decoys);
    }
}
