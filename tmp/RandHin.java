import org.openscience.cdk.io.HINReader;
import org.openscience.cdk.io.HINWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemModel;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.ChemSequence;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.Atom;
import org.openscience.cdk.SetOfMolecules;

import java.util.Random;

public class RandHin {
    
    public static void main(String[] args) {
        try {
            FileReader r = new FileReader(new File(args[0]));
            HINReader hinReader = new HINReader(r);
            ChemFile chemFile = (ChemFile)hinReader.read((ChemObject)new ChemFile());

            r.close();
            hinReader.close();

            ChemSequence chemSequence = chemFile.getChemSequence(0);
            ChemModel chemModel = chemSequence.getChemModel(0);
            SetOfMolecules som = chemModel.getSetOfMolecules();
            Molecule mol = som.getMolecule(0);

            Random rnd = new Random();
            for (int i = 0; i < mol.getAtomCount(); i++) {
                Atom a = mol.getAtomAt(i);
                a.setX3d( a.getX3d() + rnd.nextDouble() );
                a.setY3d( a.getY3d() + rnd.nextDouble() );
                a.setZ3d( a.getZ3d() + rnd.nextDouble() );
            }

            FileWriter w = new FileWriter(new File(args[0]));
            try {
                HINWriter hinWriter = new HINWriter(w);
                hinWriter.write(mol);
                hinWriter.close();
            } catch (Exception fnf) {
                System.out.println(fnf.toString());
            }        


        } catch (Exception fnf) {
            System.out.println(fnf.toString());
        }

    }
}
