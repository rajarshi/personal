package net.guha.apps.surface;

import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

import javax.vecmath.Point3d;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rguha
 * Date: Dec 6, 2007
 * Time: 3:15:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class GaussianSurface {

    IAtomContainer atomContainer;
    double spacing = 0.5;

    public GaussianSurface(IAtomContainer atomContainer) {
        this.atomContainer = atomContainer;
    }

    public void calculateSurface() {
        int natom = atomContainer.getAtomCount();

        // get extents of the molecule and eval the centroid while we're doing it

        double minx = 1E20;
        double miny = 1E20;
        double minz = 1E20;

        double maxx = -1E20;
        double maxy = -1E20;
        double maxz = -1E20;

        Point3d centroid = new Point3d(0, 0, 0);

        Iterator<IAtom> atoms = atomContainer.atoms();
        while (atoms.hasNext()) {
            IAtom atom = atoms.next();
            Point3d coord = atom.getPoint3d();

            if (coord.x > maxx) maxx = coord.x;
            if (coord.y > maxy) maxy = coord.y;
            if (coord.z > maxz) maxz = coord.z;

            if (coord.x < minx) minx = coord.x;
            if (coord.y < miny) miny = coord.y;
            if (coord.z < minz) minz = coord.z;

            centroid.x += coord.x;
            centroid.y += coord.y;
            centroid.z += coord.z;
        }

        centroid.x /= natom;
        centroid.y /= natom;
        centroid.z /= natom;

        // calculate the extents and number of grid points
        int nx = (int) ((maxx - minx) / spacing);
        int ny = (int) ((maxy - miny) / spacing);
        int nz = (int) ((maxz - minz) / spacing);

        System.out.println("num grid points = " + nx + " " + ny + " " + nz);
        System.out.println("nx*ny*nz = " + nx * ny * nz);

        // next generate the grid points
        double[][][] rValues = new double[nx][ny][nz];
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                for (int k = 0; k < nz; k++) {
                    double xpos = minx + spacing * i;
                    double ypos = miny + spacing * j;
                    double zpos = minz + spacing * k;
                                        
                }
            }
        }
        double[][][] expValues = new double[nx][ny][nz];
    }

    public static void main(String[] args) throws FileNotFoundException, CDKException {
        String filename = "/home/rguha/aspirin.sdf";
        MDLV2000Reader sdfreader = new MDLV2000Reader(new FileReader(new File(filename)));
        ChemFile chemFile = (ChemFile) sdfreader.read((ChemObject) new ChemFile());
        List containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
        IAtomContainer atomContainer = (IAtomContainer) containersList.get(0);

        GaussianSurface gs = new GaussianSurface(atomContainer);
        gs.calculateSurface();
    }

}
