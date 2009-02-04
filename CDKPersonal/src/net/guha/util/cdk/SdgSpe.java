package net.guha.util.cdk;

import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.graph.matrix.AdjacencyMatrix;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * @author Rajarshi Guha
 */
public class SdgSpe {
    private IAtomContainer atomContainer;
    private double bondLength = 1.0;

    public SdgSpe(IAtomContainer atomContainer) {
        this.atomContainer = atomContainer;
    }

    public void generateCoordinates() {

        int natom = atomContainer.getAtomCount();

        // lets get the topological distance matrix
        int[][] admat = AdjacencyMatrix.getMatrix(atomContainer);
        int[][] m = PathTools.computeFloydAPSP(admat);

        double[][] lb = new double[natom][natom];
        double[][] ub = new double[natom][natom];


    }
}
