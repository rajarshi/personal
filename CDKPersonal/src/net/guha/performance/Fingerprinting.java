package net.guha.performance;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingMDLReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * @cdk.author Rajarshi Guha
 * @cdk.svnrev $Revision: 9162 $
 */
public class Fingerprinting {

    public static void main(String[] args) throws FileNotFoundException, InterruptedException, CDKException {
        String fileName = "/home/rguha/src/java/cdk-qa/trunk/projects/zinc-structures/ZINC_subset3_3D_charged_wH_maxmin1000.sdf";

        Thread.sleep(30000);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            IteratingMDLReader reader = new IteratingMDLReader(new FileReader(new File(fileName)),
                    DefaultChemObjectBuilder.getInstance());
            Fingerprinter fprinter = new Fingerprinter();
            int n = 0;
            while (reader.hasNext()) {
                IAtomContainer molecule = (IAtomContainer) reader.next();
                fprinter.getFingerprint(molecule);                
            }
        }
        long end = System.currentTimeMillis();
        double elapsed = (end-start);
        System.out.println("elapsed = " + elapsed);
    }
}