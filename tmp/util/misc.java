package util;

import java.util.Vector;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.io.FileReader;

import org.openscience.cdk.interfaces.ChemFile;
import org.openscience.cdk.interfaces.ChemObject;
import org.openscience.cdk.interfaces.Molecule;
import org.openscience.cdk.io.ChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.interfaces.Atom;
import org.openscience.cdk.interfaces.Bond;
import org.openscience.cdk.interfaces.AtomContainer;
import org.openscience.cdk.isomorphism.mcss.RMap;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.renderer.Renderer2D;
import org.openscience.cdk.renderer.Renderer2DModel;

public class misc {
    static public int intArrayMax(int[] v) {
        // returns the index of the maximum value of the array
        int max = -99999999;
        int idx = 0;
        for (int i = 0; i < v.length; i++) {
            if (v[i] > max) {
                max = v[i];
                idx = i;
            }
        }
        return(idx);
    }

    static public AtomContainer getNeedle(AtomContainer a, AtomContainer q) {
        AtomContainer needle = new org.openscience.cdk.AtomContainer();
        Vector idlist = new Vector();

        List l = null;
        try {
        l = UniversalIsomorphismTester.getSubgraphMaps(a, q);
        } catch(Exception e) {}

        //System.out.println("Number of matched subgraphs = "+l.size());

        if (l.size() == 0) return null;
        
        for (int j = 0; j < 1; j++) {
            List maplist = (List)l.get(j);
            for (Iterator i = maplist.iterator(); i.hasNext(); ) {
                RMap rmap = (RMap)i.next();
                idlist.add( new Integer( rmap.getId1() ) );
            }
        }
        HashSet hs = new HashSet(idlist);
        for (Iterator i = hs.iterator(); i.hasNext();) {
            needle.addBond( a.getBondAt( ((Integer)i.next()).intValue() ) );
        }
        return needle;
    }

    // takes a list of file names and returns a Vector of
    // AtomContainers
    static public Vector loadMolecules(String[] args, boolean withH) {
        Vector v = new Vector();
        try {
            int i = 0;
            int j = 0;

            // load the molecule and generate 2D coordinates
            for (i = 0; i < args.length; i++) {
                File input = new File(args[i]);
                ChemObjectReader reader = new ReaderFactory().createReader(new FileReader(input));
                ChemFile content = (ChemFile)reader.read((ChemObject)new org.openscience.cdk.ChemFile());
                AtomContainer[] c = ChemFileManipulator.getAllAtomContainers(content);

                // we should do this loop in case we have files
                // that contain multiple molecules
                for (j = 0; j < c.length; j++) {

                    // get the 2D coordinates
                    StructureDiagramGenerator sdg = new StructureDiagramGenerator();
                    sdg.setMolecule((Molecule)c[j]);
                    sdg.generateCoordinates();

                    // get rid of hydrogens so that the MCSS algo does not consider them
                    AtomContainer mol = (AtomContainer)sdg.getMolecule();
                    if (!withH) {
                        Vector ra = new Vector();
                        for (j = 0; j < mol.getAtomCount(); j++) {
                            if (mol.getAtomAt(j).getSymbol().equals("H")) ra.add( mol.getAtomAt(j) );
                        }
                        for (j = 0; j < ra.size(); j++) {
                            mol.removeAtomAndConnectedElectronContainers( (Atom)ra.get(j) );
                        }
                    } 
                    v.add(mol);
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return v;
    }


static public Vector loadMoleculesSimple(String[] args, boolean withH) {
    Vector v = new Vector();
    try {
        int i = 0;
        int j = 0;

        // load the molecule and generate 2D coordinates
        for (i = 0; i < args.length; i++) {
            File input = new File(args[i]);
            ChemObjectReader reader = new ReaderFactory().createReader(new FileReader(input));
            ChemFile content = (ChemFile)reader.read((ChemObject)new org.openscience.cdk.ChemFile());
            AtomContainer[] c = ChemFileManipulator.getAllAtomContainers(content);

            // we should do this loop in case we have files
            // that contain multiple molecules
            for (j = 0; j < c.length; j++) {
                v.add(c[j]);
            }
        }
    } catch (Exception e) {
        System.out.println(e.toString());
    }
    return v;
}
}
