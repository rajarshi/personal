import org.openscience.cdk.org.interfaces.IAtomContainer;
import org.openscience.cdk.org.interfaces.IAtom;
import org.openscience.cdk.org.graph.PathTools.*;
import org.openscience.cdk.graph.matrix.AdjacencyMatrix;

import java.util.*;

public class sbdm {

    IAtomContainer local;
    int tetrangleCycle = 1000;
    
    double[][] U;
    double[][] L;
    
    public sbdm(IAtomContainer atomContainer) {
        int natom = atomContainer.getAtomCount();
        local = atomContainer.clone();

        initializeMatrices();
    }

    private initializeMatrices() {
        int natom = local.getAtomCount();
        
        U = new double[natom][natom];
        L = new double[natom][natom];

        int[][] admat = AdjacencyMatrix.getMatrix(atomContainer);
        int[][] tdistMatrix = computeFloydAPSP(admat);

        
        for (int i = 0; i < natom-1; i++) {
            for (int j = i+1; j < natom; j++) {

                IAtom atom1 = local.getAtomAt(i);
                IAtom atom2 = local.getAtomAt(j);
                List paths = getAllPaths(local, atom1, atom2);

                if (tdistMatrix[i][j] == 1 || tdistMatrix[i][j] == 2) {
                    L[i][j] = U[i][j];
                    L[j][i] = U[i][j];
                }
            }
        }
                    

    }
