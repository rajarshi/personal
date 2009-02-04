package temporary;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtom;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: rguha
 * Date: Jan 7, 2008
 * Time: 5:37:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class intvref {
    public static void main(String[] args) {
        int total = 10000;
        int n = 1000;
        IAtom[] orig = new IAtom[total];
        for (int i = 0; i < total; i++) orig[i] = DefaultChemObjectBuilder.getInstance().newAtom("C");

        System.gc();
        
        Runtime runtime = Runtime.getRuntime();
        long before = runtime.totalMemory();

//        IAtom[] refs = new IAtom[n];
//        Random r = new Random();
//        for (int i = 0; i < n; i++) refs[i] = orig[r.nextInt(total)];

        int[] ints = new int[n];
        for (int i = 0; i < n; i++) ints[i] = i;

        long after = runtime.freeMemory();
        System.out.println("Total used = " + (before-after));
        System.out.println("Per element = "+((before-after)/n));



    }
}
