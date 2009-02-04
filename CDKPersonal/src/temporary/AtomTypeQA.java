

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.InterruptableSmilesParser;

import java.io.*;

/**
 * @author rguha
 */
public class AtomTypeQA {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: AtomTypeQA SMILES_FILE");
            System.exit(-1);
        }

        InterruptableSmilesParser sp = new InterruptableSmilesParser(DefaultChemObjectBuilder.getInstance());

        String header = "cid\taAtNum\tAtSym\n";
        BufferedWriter output = new BufferedWriter(new FileWriter("atype-report.txt"));
        output.write(header);

        BufferedWriter badsmiles = new BufferedWriter(new FileWriter("smiles-failure.txt"));

        BufferedReader input = new BufferedReader(new FileReader(args[0]));
        int nmol = 0;
        int nbad = 0;
        while (true) {
            String line = input.readLine();
            if (line == null) break;

            // assuming that the only things we get are a smiles and a cid
            String[] tokens = line.trim().split("\t");
            String smiles = tokens[0];
            String title = tokens[1];

            IMolecule mol = null;
            try {
                mol = sp.parseSmiles(smiles, 5000);
            } catch (InvalidSmilesException e) {
                badsmiles.write(smiles+"\t"+title+"\n");
                nbad++;
                nmol++;
                continue;
            }

            CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(mol.getBuilder());
            int natom = mol.getAtomCount();
            for (int i = 0; i < natom; i++) {
                IAtom atom = mol.getAtom(i);
                try {
                    IAtomType type = matcher.findMatchingAtomType(mol, atom);
                } catch (CDKException e) {
                    String report = title + "\t" + i + "\t" + atom.getSymbol() + "\n";
                    output.write(report);
                }
            }
            nmol++;
            if (nmol % 1000 == 0) {
                System.out.print("\rProcessed " + nmol + " [Bad SMI = " + nbad + "]");
                output.flush();
                badsmiles.flush();
                System.out.flush();
            }
        }

        input.close();
        output.close();
        badsmiles.close();

        System.out.println("\nDone");
    }
}
