package temporary;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.descriptors.molecular.ZagrebIndexDescriptor;
import org.openscience.cdk.qsar.result.DoubleResult;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rguha
 * Date: Jan 27, 2009
 * Time: 4:34:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class tester {

    public static List<IAtomContainer> func1() throws FileNotFoundException, CDKException {
        FileReader freader = new FileReader(new File("/home/rguha/src/datasets/tutorial/big.sdf"));
        MDLV2000Reader reader = new MDLV2000Reader(freader);
        IChemFile chemFile = (IChemFile) reader.read(new ChemFile());
        List<IAtomContainer> mols = ChemFileManipulator.getAllAtomContainers(chemFile);
        System.out.println("mols.size() = " + mols.size());
        for (IAtomContainer mol : mols) {
            System.out.println("mol.getProperty(CDKConstants.TITLE) = " + mol.getProperty(CDKConstants.TITLE));
        }
        return mols;
    }

    public static void func2(List<IAtomContainer> mols) {
        ZagrebIndexDescriptor d = new ZagrebIndexDescriptor();
        DescriptorValue value = d.calculate(mols.get(154));
        DoubleResult result = (DoubleResult) value.getValue();
        System.out.println("result.doubleValue() = " + result.doubleValue());
    }

    public static void func3(List<IAtomContainer> mols) throws CDKException {
        SMARTSQueryTool sqt = new SMARTSQueryTool("C");
        sqt.setSmarts("C(=O)C");
        boolean matched = sqt.matches(mols.get(0));
        System.out.println("matched = " + matched);

        List<List<Integer>> x = sqt.getUniqueMatchingAtoms();
    }
    public static void main(String[] args) throws CDKException, FileNotFoundException {
        List<IAtomContainer> mols = func1();
        func3(mols);
    }

}
