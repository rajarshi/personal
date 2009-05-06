package net.guha.apps;

import org.openscience.cdk.smiles.smarts.parser.SMARTSParser;
import org.openscience.cdk.smiles.smarts.parser.ASTStart;
import org.openscience.cdk.smiles.smarts.parser.ParseException;
import org.openscience.cdk.smiles.smarts.parser.visitor.SmartsQueryVisitor;
import org.openscience.cdk.isomorphism.matchers.IQueryAtomContainer;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.exception.CDKException;

import java.util.List;


public class SMARTSManipulator {

    public SMARTSManipulator() {
    }

    private IQueryAtomContainer getQueryMolecule(String smarts) throws ParseException {
        SMARTSParser parser = new SMARTSParser(new java.io.StringReader(smarts));
        ASTStart ast = parser.Start();
        SmartsQueryVisitor visitor = new SmartsQueryVisitor();
        return (IQueryAtomContainer) visitor.visit(ast, null);
    }

    private IAtomContainer convertToAtomContainer(IQueryAtomContainer queryContainer) {
        IAtomContainer x = queryContainer.getBuilder().newAtomContainer();
        for (IAtom atom : queryContainer.atoms()) x.addAtom(atom);
        for (IBond bond : queryContainer.bonds()) x.addBond(bond);
        return x;
    }

    public void f1() throws ParseException, CDKException {

        IQueryAtomContainer queryTarget = getQueryMolecule("c1ccccc1C(=O)C*");
        IQueryAtomContainer queryQuery = getQueryMolecule("*C([N,S])C(=O)C");

//        IQueryAtomContainer queryTarget = getQueryMolecule("C[#6]N");
//        IQueryAtomContainer queryQuery = getQueryMolecule("CCN");

//        IQueryAtomContainer queryTarget = getQueryMolecule("c1ccccc1[N,S](=O)C*");
//        IQueryAtomContainer queryQuery = getQueryMolecule("[N,S](=O)C*");


        boolean isSubgraph = UniversalIsomorphismTester.isSubgraph(convertToAtomContainer(queryTarget), queryQuery);
        System.out.println("isSubgraph = " + isSubgraph);
        System.out.println("");

        List<IAtomContainer> mcss = UniversalIsomorphismTester.getOverlaps(
                convertToAtomContainer(queryTarget),
                convertToAtomContainer(queryQuery));
        System.out.println("mcss.size() = " + mcss.size() + "\n");
        for (IAtomContainer aMcs : mcss) {
            System.out.println("aMcs.getAtomCount() = " + aMcs.getAtomCount());
            for (IAtom matchingAtom : aMcs.atoms())
                System.out.println("matchingAtom = " + matchingAtom + "(" + matchingAtom.getSymbol() + ")");
            System.out.println("");
        }
    }

    public static void main(String[] args) throws ParseException, CDKException {
        SMARTSManipulator m = new SMARTSManipulator();
        m.f1();
    }
}
