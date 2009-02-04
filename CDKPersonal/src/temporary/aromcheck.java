package temporary;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.smiles.SmilesParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by IntelliJ IDEA.
 * User: rguha
 * Date: Dec 7, 2007
 * Time: 2:17:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class aromcheck {
    public static void main(String[] args) throws CDKException, FileNotFoundException {

        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        IAtomContainer mol = sp.parseSmiles("c1ccccc1");

        CDKHueckelAromaticityDetector.detectAromaticity(mol);

        int aromatic = 0;
        for (int i = 0; i < mol.getAtomCount(); i++)
            if (mol.getAtom(i).getFlag(CDKConstants.ISAROMATIC))
                aromatic++;
        System.out.print(mol.getProperty(CDKConstants.TITLE));
        System.out.print('\t');
        System.out.print("is aromatic\t");
        System.out.print('\t');
        System.out.print(aromatic);
        System.out.println(" aromatic atoms");

    }
}
