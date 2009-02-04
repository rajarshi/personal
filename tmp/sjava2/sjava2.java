/* LD_LIBRARY_PATH should include $RHOME/lib
 * CLASSPATH should include the jar files under 
 * $RHOME/library/SJava/library/org/omegahat/Jars
 *
 * Also need to make a jar file(s) containing 
 * $RHOME/library/SJava/library/org/omegahat/R &
 * $RHOME/library/SJava/library/org/omegahat/Interfaces hierarchies
 *
 * $RHOME/library/SJava/libs needs a soft link libSJava.so to SJava.so
 * $RHOME/library/SJava/libs needs a soft link libRInterpreter.so to SJava.so
 */
package sjava2;

import org.omegahat.R.Java.REvaluator;
//import org.omegahat.R.Java.RException;
import org.omegahat.R.Java.ROmegahatInterpreter;

import java.util.Hashtable;
import java.util.Random;

import sjava2.RLinearModelFit;
import sjava2.RLinearModelPredict;

class R {
    REvaluator e;
    ROmegahatInterpreter interp;
    R(String[] args) {
        this.interp = new ROmegahatInterpreter(ROmegahatInterpreter.fixArgs(args), false);
        this.e = new REvaluator();
        this.e.voidEval("source('sjava.R')");
        System.out.println("Init done");
    }
    REvaluator getEvaluator() {
        return(this.e);
    }
}
        
public class sjava2 {
    public sjava2() {}

    public static void doLS(REvaluator e) {
        //String[] objects = (String[]) e.call("ls", new Object[]{new Integer(2)});
        String[] objects = (String[]) e.eval("ls()");
        System.out.println("doLS: num objects = "+objects.length);
        for (int i = 0; i < objects.length; i++) {
            System.out.println(objects[i]);
        }
    }

    public static void doLM(REvaluator e) {
        e.voidEval("source('Rlm.R')");

        // each {..} is one column in R. So the first index
        // indicates cols and second index ndicates rows
        int nrow = 100;
        double[][] jm = new double[3][nrow];
        double[] km = new double[nrow];
        Random rnd = new Random(123);
        for (int i = 0; i < nrow; i++) {
            km[i] = rnd.nextGaussian();
            for (int j = 0; j < 3; j++) 
                jm[j][i] = rnd.nextGaussian();
        }

        double[][] newx = new double[3][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 3; j++) 
                newx[j][i] = rnd.nextGaussian();
        }

        Object retval = null;
        try {
            retval = e.call("buildLM", new Object[]{new String("mymodel"), jm,km});
        } catch (Exception re) {
            System.out.println(re.toString());
        }
        System.out.println("Class of return value: "+retval.getClass()); 
        RLinearModelFit slmf = (RLinearModelFit)retval;
        System.out.println("Rank = "+slmf.getRank());
        String s = "";
        double[] coeff = slmf.getCoefficients();
        for (int i = 0; i < coeff.length; i++) s += coeff[i] + " ";
        System.out.println("Coeff: "+s+"\nNow doing a prediction");
        
        try {
            retval = e.call("predictLM", new Object[]{new String("mymodel"),newx, ""});
        } catch (Exception re) {
            System.out.println(re.toString());
        }
        System.out.println("Class of return value: "+retval.getClass()); 
        RLinearModelPredict slmp = (RLinearModelPredict)retval;
        System.out.println(slmp.getDF());
        System.out.println(slmp.getResidualScale());
        doLS(e);
        //e.call("deleteLM", new Object[]{"mymodel"});
        e.eval("rm(mymodel,pos=1)");
        System.out.println("#######################");
        doLS(e);
    }
    

    public static void see(REvaluator e) {
        Object o = e.eval("summary(model)");
        System.out.println(o.getClass());
    }
    public static void doLM2(REvaluator e) {
        e.voidEval("source('Rlm.R')");

        // each {..} is one column in R. So the first index
        // indicates cols and second index ndicates rows
        int nrow = 100;
        double[][] jm = new double[3][nrow];
        double[] km = new double[nrow];
        Random rnd = new Random();
        for (int i = 0; i < nrow; i++) {
            km[i] = rnd.nextGaussian();
            for (int j = 0; j < 3; j++) 
                jm[j][i] = rnd.nextGaussian();
        }

        Object retval = null;
        try {
            retval = e.call("doLM2", new Object[]{jm,km});
        } catch (Exception re) {
            //System.out.println(re.toString());
            System.out.println("exception occured");
        }
        System.out.println("Class of return value: "+retval.getClass()); 
    }

        
        
    public static void main(String[] args){
        Object[] funArgs;
        Object value;
        String[] names;

        R robject = new R(args);
        REvaluator e = robject.getEvaluator();
        

        
        /* Don't do this! That is - a single java program
         * should only start SJava once:
         *
         * http://www.mail-archive.com/r-devel@stat.math.ethz.ch/msg05174.html
         *
        x myx = new x(args);
        REvaluator e = myx.getEvaluator();

        x myx1 = new x(args);
        REvaluator e1 = myx1.getEvaluator();
          */  

        doLM(e);
        //doLM2(e);
            
     }
}
