import org.openscience.cdk.qsar.*;
import java.util.*;

public class desc {

  public static void main(String[] args) {

    String[] jarFiles = {
      "/home/rajarshi/src/java/cdk/trunk/cdk/dist/jar/cdk-svn-20060706.jar"
    };
    List classNames = DescriptorEngine.getDescriptorClassNameByInterface("IMolecularDescriptor", jarFiles);
    List cn1 = DescriptorEngine.getDescriptorClassNameByPackage("org.openscience.cdk.qsar.descriptors.molecular", jarFiles);
    
    Collections.sort(classNames);
    Collections.sort(cn1);

    if (classNames.size() != cn1.size()) {
      System.out.println("Gaar");
      System.exit(0);
    }
    for (int i = 0; i < cn1.size(); i++) {
      String s1 = (String)classNames.get(i);
      String s2 = (String)cn1.get(i);
      if (!s1.equals(s2)) System.out.println("screwed");
    }
  }
}

