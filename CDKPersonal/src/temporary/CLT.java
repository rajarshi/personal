package temporary;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * @author Rajarshi Guha
 */

final class JarResource {
    // external debug flag
    public boolean debugOn = false;

    // jar resource mapping tables
    private Hashtable htSizes = new Hashtable();

    public Hashtable getHtJarContents() {
        return htJarContents;
    }

    private Hashtable htJarContents = new Hashtable();

    // a jar file
    private String jarFileName;

    /**
     * creates a JarResources. It extracts all resources from a Jar
     * into an internal hashtable, keyed by resource names.
     *
     * @param jarFileName a jar or zip file
     */
    public JarResource(String jarFileName) throws FileNotFoundException {
        this.jarFileName = jarFileName;
        init();
    }

    /**
     * Extracts a jar resource as a blob.
     *
     * @param name a resource name.
     */
    public byte[] getResource(String name) {
        return (byte[]) htJarContents.get(name);
    }

    /**
     * initializes internal hash tables with Jar file resources.
     */
    private void init() throws FileNotFoundException {
        File f = new File(jarFileName);
        if (!f.exists()) throw new FileNotFoundException("Specified jar file does not exist");

        try {
            // extracts just sizes only.
            ZipFile zf = new ZipFile(jarFileName);
            Enumeration e = zf.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) e.nextElement();
                if (debugOn) {
                    System.out.println(dumpZipEntry(ze));
                }
                htSizes.put(ze.getName(), new Integer((int) ze.getSize()));
            }
            zf.close();

            // extract resources and put them into the hashtable.
            FileInputStream fis = new FileInputStream(jarFileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ZipInputStream zis = new ZipInputStream(bis);
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }
                if (debugOn) {
                    System.out.println(
                            "ze.getName()=" + ze.getName() + "," + "getSize()=" + ze.getSize()
                    );
                }
                int size = (int) ze.getSize();
                // -1 means unknown size.
                if (size == -1) {
                    size = ((Integer) htSizes.get(ze.getName())).intValue();
                }
                byte[] b = new byte[size];
                int rb = 0;
                int chunk;
                while ((size - rb) > 0) {
                    chunk = zis.read(b, rb, size - rb);
                    if (chunk == -1) {
                        break;
                    }
                    rb += chunk;
                }
                // add to internal resource hashtable
                htJarContents.put(ze.getName(), b);
                if (debugOn) {
                    System.out.println(
                            ze.getName() + "  rb=" + rb +
                                    ",size=" + size +
                                    ",csize=" + ze.getCompressedSize()
                    );
                }
            }
        } catch (NullPointerException e) {
            System.out.println("done.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Dumps a zip entry into a string.
     *
     * @param ze a ZipEntry
     */
    private String dumpZipEntry(ZipEntry ze) {
        StringBuffer sb = new StringBuffer();
        if (ze.isDirectory()) {
            sb.append("d ");
        } else {
            sb.append("f ");
        }
        if (ze.getMethod() == ZipEntry.STORED) {
            sb.append("stored   ");
        } else {
            sb.append("defalted ");
        }
        sb.append(ze.getName());
        sb.append("\t");
        sb.append("").append(ze.getSize());
        if (ze.getMethod() == ZipEntry.DEFLATED) {
            sb.append("/").append(ze.getCompressedSize());
        }
        return (sb.toString());
    }

}

class MyClassLoader extends ClassLoader {
    private Hashtable classes = new Hashtable();
    private JarResource jr;

    public MyClassLoader() {
        super(MyClassLoader.class.getClassLoader());
    }

    public void setJR(JarResource jr) {
        this.jr = jr;
    }

    /**
     * This is a simple version for external clients since they
     * will always want the class resolved before it is returned
     * to them.
     */
    public Class loadClass(String className) throws ClassNotFoundException {
        return (loadClass(className, false));
    }

    public synchronized Class loadClass(String className, boolean resolveIt)
            throws ClassNotFoundException {
        Class result;
        byte classData[];

        System.out.println("        >>>>>> Load class : " + className);

        /* Check our local cache of classes */
        result = (Class) classes.get(className);
        if (result != null) {
            System.out.println("        >>>>>> returning cached result.");
            return result;
        }

        /* Check with the primordial class loader */
        try {
            result = super.findSystemClass(className);
            System.out.println("        >>>>>> returning system class (in CLASSPATH).");
            return result;
        } catch (ClassNotFoundException e) {
            System.out.println("        >>>>>> Not a system class.");
        }

        /* Try to load it from our repository */
        //classData = getClassImplFromDataBase(className);
        classData = jr.getResource(className);
        if (classData == null) {
            //throw new ClassNotFoundException();
        }

        /* Define it (parse the class file) */

        try {
        result = defineClass(classData, 0, classData.length);
        } catch (NoClassDefFoundError e) {
            System.out.println("Got a NoClassDefFoundError");
        }
        if (result == null) {
            throw new ClassFormatError();
        }

        if (resolveIt) {
            resolveClass(result);
        }

        classes.put(className, result);
        System.out.println("        >>>>>> Returning newly loaded class.");
        return result;
    }
}

