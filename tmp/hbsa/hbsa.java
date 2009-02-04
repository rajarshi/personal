import joelib2.smarts.*;
import joelib2.io.*;
import java.io.*;

import joelib2.molecule.Atom;
import joelib2.molecule.BasicConformerMolecule;
import joelib2.molecule.Bond;
import joelib2.molecule.Molecule;

import joelib2.util.iterator.AtomIterator;
import joelib2.util.iterator.NbrAtomIterator;
import wsi.ra.tool.BasicResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class hbsa {

    public static void main(String args[]) {
        /*
        String smartsPattern = "c1ccccc1";
        JOESmartsPattern smarts = new JOESmartsPattern();
        */

        String molURL=args[1];
        BasicIOType inType = BasicIOTypeHolder.instance().getIOType("SDF");

        byte[] bytes = BasicResourceLoader.instance()
            .getBytesFromResourceLocation(molURL);
        ByteArrayInputStream sReader = new ByteArrayInputStream(bytes);

        // create simple reader
        BasicReader reader = null;

        try {
            reader = new BasicReader(sReader, inType);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        Molecule mol = new BasicConformerMolecule(inType, outType);
    }
}
