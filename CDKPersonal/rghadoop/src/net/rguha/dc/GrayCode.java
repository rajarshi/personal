// $Id: GrayCode.java 2707 2009-06-22 09:29:24Z nguyenda $

package net.rguha.dc;

import java.util.Observer;
import java.util.Observable;

/**
 * Gray code.
 *
 * @author Trung-Dac Nguyenda   
 */
public class GrayCode extends Observable {

    private int maxiter = 0;
    private int N, k;
    private int[] n, g, u, c;

    public GrayCode(int N, int k) {
        this.N = N;
        this.k = k;
        init();
    }

    protected void init() {
        n = new int[k + 1];
        g = new int[k + 1];
        u = new int[k + 1];
        c = new int[k]; // copy of g

        for (int i = 0; i <= k; ++i) {
            g[i] = 0;
            u[i] = 1;
            n[i] = N;
        }
    }

    public void generate() {
        for (int i, j, iter = 0; g[k] == 0; ++iter) {
            System.arraycopy(g, 0, c, 0, k);

            setChanged();
            notifyObservers(c);

            i = 0;
            j = g[0] + u[0];
            while ((j >= n[i]) || (j < 0)) {
                u[i] = -u[i];
                ++i;
                j = g[i] + u[i];
            }
            g[i] = j;

            if (countObservers() == 0
                    || (maxiter > 0 && iter >= maxiter)) {
                break;
            }
        }
    }

    public void setMaxIter(int maxiter) {
        this.maxiter = maxiter;
    }

    public int getMaxIter() {
        return maxiter;
    }

    public static GrayCode createBinaryGrayCode(int size) {
        return new GrayCode(2, size);
    }

    public static void main(String[] argv) throws Exception {
        if (argv.length < 2) {
            System.out.println("Usage: GrayCode N k\n");
            System.exit(1);
        }

        int N = Integer.parseInt(argv[0]);
        int k = Integer.parseInt(argv[1]);

        System.out.println("Gray Code (" + N + "," + k + ")");
        GrayCode g = new GrayCode(N, k);
        g.addObserver(new Observer() {
            public void update(Observable o, Object arg) {
                int[] c = (int[]) arg;
                System.out.print(c[0]);
                for (int i = 1; i < c.length; ++i) {
                    System.out.print(" " + c[i]);
                }
                System.out.println();
            }
        });
        g.generate();
    }
}
