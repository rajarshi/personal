
import javax.vecmath.Point3d;
import java.lang.Math;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.Atom;
import org.openscience.cdk.config.AtomTypeFactory;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.tools.LoggingTool;
import org.openscience.cdk.exception.CDKException;


import java.io.File;
import java.io.FileReader;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.Bond;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.io.ChemObjectReader;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.openscience.cdk.io.ReaderFactory;

import org.openscience.cdk.geometry.surface.NumericalSurface;
    
/*                   this     pearlman(savol) pymol (on the PDB file)
 * gravindex.hin     461.67   459.48          445.55
 * surftst.hin       268.09   274.37          262.69
 */
        
public class surftest {
    public static void main(String[] args) {
        String filename = args[0];
        AtomContainer mol = null;
        try {
            File input = new File(filename);
            ChemObjectReader reader = new ReaderFactory().createReader(new FileReader(input));
            ChemFile content = (ChemFile)reader.read((ChemObject)new ChemFile());
            AtomContainer[] containers = ChemFileManipulator.getAllAtomContainers(content);
            mol = containers[0];
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        NumericalSurface surf = new NumericalSurface(mol, 1.4, 3);
        surf.calculateSurface();
        /*
        Point3d[] pts = surf.getAllSurfacePoints();
        for (int i = 0; i < pts.length; i++) {
            System.out.println("H "+pts[i].x+" "+pts[i].y+" "+pts[i].z);
        }
        */
        System.out.println("Total SA = "+surf.getTotalSurfaceArea());
        double[] areas = surf.getAllSurfaceAreas();
        //for (int i = 0; i < areas.length; i++)
            //System.out.println("Atom "+(i+1)+": "+areas[i]);
    }
}
