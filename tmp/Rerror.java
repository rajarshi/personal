
import org.omegahat.R.Java.ROmegahatInterpreter;
import org.omegahat.R.Java.REvaluator;


public class Rerror {

  static public void main(String[] args) {
      ROmegahatInterpreter interp = new ROmegahatInterpreter(ROmegahatInterpreter.fixArgs(args), false);
      REvaluator e = new REvaluator();


      try {
          e.call("nonexistantFunction");
      } catch(Exception ex) {
	  System.err.println("Caught the non-available function exception");
	  System.err.println(ex);
	  ex.printStackTrace();
      }


      try {
          e.call("strsplit", new Object[] { new Integer(1)});
      } catch(Exception ex) {
	  System.err.println(ex);
	  ex.printStackTrace();
      }

      try {
          e.eval("sum(x)");
      } catch(Exception ex) {
	  System.err.println(ex);
 	  ex.printStackTrace();
      }


      System.err.println("Finished with error handling");
   }

}
