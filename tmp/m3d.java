import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.io.IChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.openscience.cdk.modeling.builder3d.ModelBuilder3D;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.HydrogenAdder;
import org.openscience.cdk.tools.LoggingTool;
import org.openscience.cdk.io.MDLWriter;
import java.io.FileWriter;
import java.io.File;


public class m3d {

    public static IAtomContainer get3DCoordinates(Molecule molecule, String forceField) {
    if (forceField == null || forceField.equals("")) {
        forceField = "mm2";
    }
    ModelBuilder3D mb3d = new ModelBuilder3D();
    HydrogenAdder hAdder=new HydrogenAdder();
    try {
        hAdder.addExplicitHydrogensToSatisfyValency((IMolecule)molecule);
        mb3d.setForceField(forceField);
        mb3d.setMolecule((IMolecule)molecule, false);
        int x = mb3d.generate3DCoordinates();
        System.out.println(x);
    } catch (Exception e) {
        System.out.println("Problem generating coordinates\n\n"+e.getMessage());
        return(null);
    }
    IMolecule tmp = mb3d.getMolecule();
    //Molecule newMolecule = (Molecule)DefaultChemObjectBuilder.getInstance().newMolecule(tmp);
    return((IAtomContainer)tmp);
}

    public static void main(String[] args) {
        try{
            SmilesParser sp = new SmilesParser();
            IMolecule mol = sp.parseSmiles("CCCCC");
            IAtomContainer ac = get3DCoordinates((Molecule)mol, "");
            FileWriter w = new FileWriter(new File("molecule.mol"));
try {
   MDLWriter mw = new MDLWriter(w);
   mw.write(ac);
   mw.close();
} catch (Exception e) {
   System.out.println(e.toString());
}
            System.out.println(ac);
        } catch (Exception exc)
        {
            exc.printStackTrace();
        }
    }
}
