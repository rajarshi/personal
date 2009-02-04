import java.util.jar.*;
import java.util.zip.ZipEntry;
import java.util.*;
import java.io.IOException;
import java.io.File;

public class jarclass {

    public static void main(String[] args) {
        String classPath = System.getProperty("java.class.path");
        String[] jars = classPath.split(File.pathSeparator);
        ArrayList classlist = new ArrayList();
        System.out.println("Scanning "+jars.length+" jar files");
        try {
            for (String jarFileName : jars) {
                //System.out.println(jarFileName);
                JarFile j = new JarFile(jarFileName);
                Enumeration e = j.entries();
                while (e.hasMoreElements()) {
                    JarEntry je = (JarEntry) e.nextElement();

                    if (!je.toString().contains("Descriptor.class")) continue;
                    if (!je.toString().contains("org/openscience/cdk/qsar")) continue;
                    //classlist.add(je);

                    String className = je.toString().replace('/','.').replaceAll(".class","");
                    try {
                        Class classobj = Class.forName(className);
                        Class[] interfaces = classobj.getInterfaces();
                        for (Class iface : interfaces) {
                            if (iface.getName().equals("org.openscience.cdk.qsar.Descriptor"))
                                classlist.add(classobj);
                        }
                    } catch (ClassNotFoundException excp) {
                        excp.printStackTrace();
                    }
                    //System.out.println(className);

                }
            }
        }catch (IOException e) {
        }
        System.out.println("Got "+classlist.size()+" descriptor classes");
    }
}
