package util;

public class x {
    private double[][] vectorToMatrix(double[] v, int nrow, int ncol) {
        double[][] m = new double[nrow][ncol];
        for (int i = 0; i < ncol; i++) {
            for (int j = 0; j < nrow; j++) {
                m[j][i] = v[j + i*nrow];
            }
        }
        return(m);
    }

    public x(int nrow, double[] b) {
        int nelem = b.length;
        int ncol = nelem / nrow;
        System.out.println(nrow+" "+ncol);
        double[][] mb = vectorToMatrix(b, nrow, ncol);
        for (int i = 0; i < nrow; i++) {
            for (int j = 0; j < ncol; j++) {
                System.out.print(mb[i][j]+" ");
            }
            System.out.println();
        }
    }
}
