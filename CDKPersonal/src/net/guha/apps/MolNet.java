package net.guha.apps;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.fingerprint.IFingerprinter;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSMILESReader;
import org.openscience.cdk.similarity.Tanimoto;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.graph.FruchtermanReingoldLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.collections.IntIterator;
import prefuse.visual.VisualItem;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * A one line summary.
 *
 * @author Rajarshi Guha
 */
public class MolNet {
    protected static final String SRC = Graph.DEFAULT_SOURCE_KEY;
    protected static final String TRG = Graph.DEFAULT_TARGET_KEY;


    List<IAtomContainer> mols;
    List<BitSet> fps;
    IFingerprinter fingerprinter = null;
    float sims[][];
    float cutoff = 0.5f;

    public static void main(String[] args) throws FileNotFoundException, CDKException {
        IteratingSMILESReader reader = new IteratingSMILESReader(new FileReader("test200.smi"),
                DefaultChemObjectBuilder.getInstance());
        List<IAtomContainer> mols = new ArrayList<IAtomContainer>();
        while (reader.hasNext()) {
            mols.add((IAtomContainer) reader.next());
        }
        System.out.println("Read in " + mols.size() + " molecules");

        MolNet mn = new MolNet(mols);
        mn.setCutoff(0.6f);

    }

    public void setCutoff(float cutoff) {
        this.cutoff = cutoff;
    }

    public MolNet(List<IAtomContainer> mols) throws CDKException {
        this.mols = mols;
        fingerprinter = new Fingerprinter();
        fps = new ArrayList<BitSet>();

        int row_id = -1;
        Table nodeTable = new Table();
        nodeTable.addColumn("MOLID", String.class);
        nodeTable.addColumn("MOL", IAtomContainer.class);

        for (IAtomContainer mol : mols) {
            String title = (String) mol.getProperty(CDKConstants.TITLE);
            fps.add(fingerprinter.getFingerprint(mol));

            row_id = nodeTable.addRow();
            nodeTable.set(row_id, "MOLID", title);
            nodeTable.set(row_id, "MOL", mol);
        }
        System.out.println("Generated node table with " + nodeTable.getRowCount() + " rows");

        // start on the edges
        Table edgeTable = new Table();
        edgeTable.addColumn(SRC, int.class);
        edgeTable.addColumn(TRG, int.class);
        int nr_id = -1;
        for (int i = 0; i < mols.size() - 1; i++) {
            for (int j = i + 1; j < mols.size(); j++) {
                float sim = Tanimoto.calculate(fps.get(i), fps.get(j));
                if (sim > cutoff) {
                    String ti = (String) mols.get(i).getProperty(CDKConstants.TITLE);
                    String tj = (String) mols.get(j).getProperty(CDKConstants.TITLE);

                    row_id = edgeTable.addRow();
                    edgeTable.set(row_id, SRC, getRowForTitle(nodeTable, ti));
                    edgeTable.set(row_id, TRG, getRowForTitle(nodeTable, tj));
                }
            }
        }
        System.out.println("Generated " + edgeTable.getRowCount() + " edges with cutoff = " + cutoff);

        Graph g = new Graph(nodeTable, edgeTable, false);
        Visualization vis = new Visualization();
        vis.add("graph", g);

        LabelRenderer r = new LabelRenderer("MOLID");
        r.setRoundedCorner(8, 8); // round the corners
        vis.setRendererFactory(new DefaultRendererFactory(r));
        int[] palette = new int[]{
                ColorLib.rgb(255, 180, 180), ColorLib.rgb(190, 190, 255)
        };
// map nominal data values to colors using our provided palette
//        DataColorAction fill = new DataColorAction("graph.nodes", "gender",
//                Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
// use black for node text
        ColorAction text = new ColorAction("graph.nodes",
                VisualItem.TEXTCOLOR, ColorLib.gray(0));
// use light grey for edges
        ColorAction edges = new ColorAction("graph.edges",
                VisualItem.STROKECOLOR, ColorLib.gray(200));

// create an action list containing all color assignments
        ActionList color = new ActionList();
//        color.add(fill);
        color.add(text);
        color.add(edges);

        ActionList layout = new ActionList(Activity.INFINITY);
//        layout.add(new ForceDirectedLayout("graph"));
        layout.add(new FruchtermanReingoldLayout("graph"));
        layout.add(new RepaintAction());


        vis.putAction("color", color);
        vis.putAction("layout", layout);

        Display display = new Display(vis);
        display.setSize(720, 500); // set display size
        display.addControlListener(new DragControl()); // drag items around
        display.addControlListener(new PanControl());  // pan with background left-drag
        display.addControlListener(new ZoomControl());

        JFrame frame = new JFrame("prefuse example");
// ensure application exits when window is closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(display);
        frame.pack();           // layout components in window
        frame.setVisible(true); // show the window

        vis.run("color");  // assign the colors
        vis.run("layout"); // start up the animated layout
    }

    private int getRowForTitle(Table t, String title) {
        IntIterator iter = t.rows();
        while (iter.hasNext()) {
            int rowid = iter.nextInt();
            String molid = (String) t.get(rowid, "MOLID");
            if (molid.equals(title)) return rowid;
        }
        return -1;
    }

    private void evaluateFingerprints() throws CDKException {
        fps = new ArrayList<BitSet>();
        for (IAtomContainer mol : mols) {
            fps.add(fingerprinter.getFingerprint(mol));
        }
    }

    private void evaluateSimilarities() throws CDKException {
        sims = new float[mols.size()][mols.size()];
        for (int i = 0; i < mols.size() - 1; i++) {
            for (int j = i + 1; j < mols.size(); j++) {
                sims[i][j] = Tanimoto.calculate(fps.get(i), fps.get(j));
                sims[j][i] = sims[i][j];
            }
        }
    }

    class MoleculeNode {
        IAtomContainer mol;
        BitSet fingerprint;
        String title;
        double activity;

        public IAtomContainer getMol() {
            return mol;
        }

        public void setMol(IAtomContainer mol) {
            this.mol = mol;
        }

        public BitSet getFingerprint() {
            return fingerprint;
        }

        public void setFingerprint(BitSet fingerprint) {
            this.fingerprint = fingerprint;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public double getActivity() {
            return activity;
        }

        public void setActivity(double activity) {
            this.activity = activity;
        }

        MoleculeNode(IAtomContainer mol, BitSet fingerprint, double activity) {
            this.mol = mol;

            this.fingerprint = fingerprint;
            this.title = (String) mol.getProperty(CDKConstants.TITLE);
            this.activity = activity;
        }
    }
}
