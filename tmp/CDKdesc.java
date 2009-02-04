import org.openscience.cdk.Molecule;
import org.openscience.cdk.Atom;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemSequence;
import org.openscience.cdk.ChemModel;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.qsar.*;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.CMLWriter;
import org.openscience.cdk.io.listener.PropertiesListener;
import java.util.*;
import java.io.*;

public class CDKdesc {
    PropertiesListener propsListener;
    public CDKdesc() {};
    public String getDescriptors(String cml, String types) throws CDKException {
        DescriptorEngine de = null;

        // Convert the String representation to a Molecule
        StringReader reader = new StringReader(cml);
        CMLReader cmlr = new CMLReader(reader);
        ChemFile chemFile = (ChemFile) cmlr.read((ChemObject)new ChemFile());
        ChemSequence chemSequence = chemFile.getChemSequence(0);
        ChemModel chemModel = chemSequence.getChemModel(0);
        Molecule molecule = chemModel.getSetOfMolecules().getMolecule(0);
        if (types.length() == 0 || types.equalsIgnoreCase("all")) {
            de = new DescriptorEngine();
        } else {
            // split the comma seperated list 
            String[] typelist = types.split(",");
            de = new DescriptorEngine(typelist);
        }
        de.process(molecule);


        Properties props = new Properties();
        props.setProperty("CMLIDs", "false");
        props.setProperty("NamespacedOutput", "false");
        props.setProperty("XMLDeclaration", "false");
        propsListener = new PropertiesListener(props);


        StringWriter writer = new StringWriter();
        try {
        printCMLHeader(writer);
        printCMLMolecule(writer, molecule);
        printCMLFooter(writer);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return(writer.toString());
    }
    private void printCMLHeader(Writer writer) throws IOException {
        writer.write("<?xml version=\"1.0\"?>\n");
        writer.write("<list\n");
        writer.write("  xmlns=\"http://www.xml-cml.org/schema/cml2/core\"\n");
        writer.write("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        writer.write("  xsi:schemaLocation=\"http://www.xml-cml.org/schema/cml2/core cmlAll4.4.xsd\">\n");
        writer.flush();
    }
    private void printCMLMolecule(Writer writer, Molecule molecule) throws Exception {
        StringWriter stringWriter = new StringWriter();
        CMLWriter cmlWriter = new CMLWriter(stringWriter);
        cmlWriter.addChemObjectIOListener(propsListener);
        cmlWriter.write(molecule);
        cmlWriter.close();
        writer.write(stringWriter.toString());
        writer.flush();
    }
    private void printCMLFooter(Writer writer) throws IOException {
        writer.write("</list>\n");
        writer.close();
    }
    public static String readFile(String inFileName) throws IOException {
        FileReader r = null;
        try {
            r = new FileReader(new File(inFileName));
            StringBuffer sb = new StringBuffer();
            char[] b = new char[8192];
            int n;
            while ((n = r.read(b)) > 0) { sb.append(b, 0, n); }
            return sb.toString();
        } finally {
            try {
                r.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }    
    // for testing purposes
    public static void main(String[] args) {

        String testcml = null;
        String type = args[1];

        try {
            testcml = readFile(args[0]);
        } catch (Exception e) {
            System.out.println(e);
        }

        try {
            CDKdesc cdkd = new CDKdesc();
            String x = cdkd.getDescriptors(testcml,type);
            System.out.println(x);
        } catch (CDKException cdke) {
            System.out.println(cdke.toString());
        }
    }
}


