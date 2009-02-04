import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Atom;
import java.util.Arrays;

public class perf {

  public static void main(String[] args) {
    AtomContainer ac = new AtomContainer();
    for (int i = 0; i < 100; i++) {
      ac.addAtom(new Atom("C"));
    }

    Atom atom = new Atom("C");
    //Arrays.binarySearch((Object[])ac.getAtoms(), (Object)atom);
    Arrays.sort(ac.getAtoms());
  }
}
