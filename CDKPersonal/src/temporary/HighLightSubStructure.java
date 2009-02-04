package temporary;

import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.config.Elements;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.mcss.RMap;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.Renderer2D;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.smiles.SmilesParser;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;


class displaySubstructure extends JPanel {
    IAtomContainer localhaystack;

    Renderer2DModel r2dm;
    Renderer2D renderer;

    public displaySubstructure(IAtomContainer haystack, IAtomContainer needle) {
        Hashtable ht = null;
        r2dm = new Renderer2DModel();
        renderer = new Renderer2D(r2dm);
        Dimension screenSize = new Dimension(600, 400);
        setPreferredSize(screenSize);
        r2dm.setBackgroundDimension(screenSize);
        setBackground(r2dm.getBackColor());

        localhaystack = haystack;

        try {
            r2dm.setDrawNumbers(false);
            r2dm.setUseAntiAliasing(true);
            r2dm.setShowImplicitHydrogens(true);
            r2dm.setShowAromaticity(true);
            r2dm.setColorAtomsByType(false);
            r2dm.setSelectedPartColor(Color.green);
            r2dm.setSelectedPart(needle);

            GeometryTools.translateAllPositive(haystack, r2dm.getRenderingCoordinates());
            GeometryTools.scaleMolecule(haystack, getPreferredSize(), 0.8, r2dm.getRenderingCoordinates());
            GeometryTools.center(haystack, getPreferredSize(), r2dm.getRenderingCoordinates());
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(this);
        frame.pack();
        frame.setVisible(true);

    }

    public void paint(Graphics g) {
        super.paint(g);
        renderer.paintMolecule(localhaystack, (Graphics2D) g, false, true);
    }

}

public class HighLightSubStructure {

    private static IAtomContainer getBenzeneFragment() {
        IAtomContainer q = DefaultChemObjectBuilder.getInstance().newAtomContainer();
        q.addAtom(new Atom(Elements.CARBON));
        q.addAtom(new Atom(Elements.CARBON));
        q.addAtom(new Atom(Elements.CARBON));
        q.addAtom(new Atom(Elements.CARBON));
        q.addAtom(new Atom(Elements.CARBON));
        q.addAtom(new Atom(Elements.CARBON));
        q.addBond(0, 1, CDKConstants.BONDORDER_DOUBLE);
        q.addBond(1, 2, CDKConstants.BONDORDER_DOUBLE);
        q.addBond(2, 3, CDKConstants.BONDORDER_DOUBLE);
        q.addBond(3, 4, CDKConstants.BONDORDER_DOUBLE);
        q.addBond(4, 5, CDKConstants.BONDORDER_DOUBLE);
        q.addBond(5, 0, CDKConstants.BONDORDER_DOUBLE);
        return q;
    }

    private static IAtomContainer getPrimaryAmineFragment() {
        IAtomContainer q = DefaultChemObjectBuilder.getInstance().newAtomContainer();
        q.addAtom(new Atom("N"));
        q.addAtom(new Atom("H"));
        q.addAtom(new Atom("H"));
        q.addBond(0, 1, CDKConstants.BONDORDER_SINGLE);
        q.addBond(0, 2, CDKConstants.BONDORDER_SINGLE);
        return q;
    }

    private static IAtomContainer getSecondaryAmineFragment() {
        IAtomContainer q = DefaultChemObjectBuilder.getInstance().newAtomContainer();
        q.addAtom(new Atom("N"));
        q.addAtom(new Atom("H"));
        q.addBond(0, 1, CDKConstants.BONDORDER_SINGLE);
        return q;
    }

    private static IAtomContainer getTertiaryAmineFragment() {
        IAtomContainer q = DefaultChemObjectBuilder.getInstance().newAtomContainer();
        q.addAtom(new Atom("N"));
        q.addAtom(new Atom("C"));
        q.addAtom(new Atom("C"));
        q.addAtom(new Atom("C"));
        q.addBond(0, 1, CDKConstants.BONDORDER_SINGLE);
        q.addBond(0, 2, CDKConstants.BONDORDER_SINGLE);
        q.addBond(0, 3, CDKConstants.BONDORDER_SINGLE);
        return q;
    }

    public static void main(String[] args) {

//        if (args.length != 1) {
//            System.out.println("Must specify a SMILES string ");
//            System.exit(0);
//        }
//        String filename = args[0];
        String filename = "CC(CCN(C)(C))CCC";
        try {

            SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
            IAtomContainer molecule = sp.parseSmiles(filename);

            StructureDiagramGenerator sdg = new StructureDiagramGenerator();
            sdg.setMolecule((IMolecule) molecule);
            sdg.generateCoordinates();
            IMolecule mol = sdg.getMolecule();

            // Create a fragment to search for
            IAtomContainer q = getTertiaryAmineFragment();
//            IAtomContainer q = getBenzeneFragment();

            // find all subgraphs of the original molecule matching the fragment
            List l = UniversalIsomorphismTester.getSubgraphMaps(mol, q);
            System.out.println("Number of matched subgraphs = " + l.size());

            AtomContainer needle = new AtomContainer();
            Vector<Integer> idlist = new Vector<Integer>();

            // get the ID's (corresponding to the serial number of the Bond object in
            // the AtomContainer for the supplied molecule) of the matching bonds
            // (there will be repeats)
            for (Object aL : l) {
                List maplist = (List) aL;
                for (Object i : maplist) {
                    idlist.add(((RMap) i).getId1());
                }
            }

            // get a unique list of bond ID's and add them to an AtomContainer
            HashSet<Integer> hs = new HashSet<Integer>(idlist);
            for (Integer h : hs) needle.addBond(mol.getBond(h));

            // show the substructure. Enjoy :)
            new displaySubstructure(mol, needle);

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

