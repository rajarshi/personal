package net.guha.apps.rest;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.qsar.DescriptorEngine;
import org.openscience.cdk.qsar.DescriptorSpecification;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.IMolecularDescriptor;
import org.openscience.cdk.qsar.result.*;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import net.guha.util.cdk.Base64;


public class Utils {
    /**
     * Get a list of the descriptor names.
     * <p/>
     * This can be used to obtain the names of the descriptors and then evaluate
     * a subset of them. It is possible to obtain the names of descriptors from specifc
     * classes, depending on the value of the argument.
     * <p/>
     * <b>Note</b>: For now we ignore the protein descriptors.
     *
     * @param type May have the values of: "", "all", "topological", "geometrical", "constitutional",
     *             "electronic", "hybrid". If an empty string or 'all" is specified, then all available descriptor
     *             names are returned.
     * @return A string array containing the names of the descriptors
     * @throws org.openscience.cdk.exception.CDKException
     *                                        if an invalid descriptor type is specified
     * @throws java.net.MalformedURLException if there is a problem internally
     */
    public static String[] getAvailableDescriptorNames(String type) throws CDKException, MalformedURLException {

        String[] validTypes = {"", "all", "topological", "constitutional", "geometrical", "electronic", "hybrid"};
        boolean isValidType = false;
        for (String s : validTypes) {
            if (type.equals(s)) {
                isValidType = true;
                break;
            }
        }
        if (!isValidType) throw new CDKException("Invalid descriptor type specified");

        DescriptorEngine descriptorEngine = new DescriptorEngine(DescriptorEngine.MOLECULAR);
        List classNames = descriptorEngine.getDescriptorClassNames();
        List<String> validNames = new ArrayList<String>();
        for (Object o : classNames) {
            String className = (String) o;

            String[] dictClasses = descriptorEngine.getDictionaryClass(className);
            for (String dictClass : dictClasses) {
                if (dictClass.equals("proteinDescriptor")) continue;
                if (type.equals("") || type.equals("all")) {
                    validNames.add(className);
                    break;
                }
                if (dictClass.equals(type + "Descriptor")) {
                    validNames.add(className);
                    break;
                }
            }
        }
        String[] ret = new String[validNames.size()];
        for (int i = 0; i < validNames.size(); i++) ret[i] = validNames.get(i);
        return ret;
    }

    public static String getDescriptorSpecifications(String[] classNames) throws MalformedURLException, CDKException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (classNames == null || classNames.length == 0) classNames = getAvailableDescriptorNames("");
        Element root = new Element("DescriptorSpecificationList");
        for (String className : classNames) {
            Class klass = Class.forName(className);
            IMolecularDescriptor descriptor = (IMolecularDescriptor) klass.newInstance();
            DescriptorSpecification spec = descriptor.getSpecification();

            Element element = new Element("DescriptorSpecification");
            element.addAttribute(new Attribute("class", className));

            Element impid = new Element("ImplementationIdentifier");
            impid.appendChild(spec.getImplementationIdentifier());

            Element imptitle = new Element("ImplementationTitle");
            imptitle.appendChild(spec.getImplementationTitle());

            Element impvendor = new Element("ImplementationVendor");
            impvendor.appendChild(spec.getImplementationVendor());

            Element specref = new Element("SpecificationReference");
            specref.appendChild(spec.getSpecificationReference());

            element.appendChild(specref);
            element.appendChild(imptitle);
            element.appendChild(impid);
            element.appendChild(impvendor);

            root.appendChild(element);
        }

        Document descriptorDoc = new Document(root);
        return descriptorDoc.toXML();

    }

    public static String evaluateDescriptors(String[] classNames, String molecule) throws CDKException, ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {
        IAtomContainer atomContainer = null;
        boolean molWasParsed = false;
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        try {
            atomContainer = sp.parseSmiles(molecule);
            molWasParsed = true;
        } catch (InvalidSmilesException e) {
            molWasParsed = false;
        }

        // if we couldn't parse the SMILES, we try it as an SDF
        if (!molWasParsed) {
            MDLV2000Reader reader = new MDLV2000Reader(new StringReader(molecule));
            ChemFile chemFile = null;
            try {
                chemFile = (ChemFile) reader.read((ChemObject) new ChemFile());
            } catch (CDKException e) {
                throw new CDKException("Could not parse specified molecule", e);
            }
            List containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
            if (containersList.size() != 1) throw new CDKException("Only one molecule can be specified in SD format");
            else atomContainer = (IAtomContainer) containersList.get(0);
        }

        // create our descriptor document
        Element root = new Element("DescriptorList");

        for (String className : classNames) {
            // get the abbreviated class name for added info
            String[] tokens = className.split("\\.");
            String descName = tokens[tokens.length - 1];

            // create the descriptor class
            Class klass = Class.forName(className);
            IMolecularDescriptor descriptor = (IMolecularDescriptor) klass.newInstance();
            DescriptorValue descriptorValue;
            descriptorValue = descriptor.calculate(atomContainer);

            // get values and names and add them to our XML document
            IDescriptorResult descriptorResult = descriptorValue.getValue();
            String[] names = descriptorValue.getNames();

            if (descriptorResult instanceof IntegerResult) {
                IntegerResult result = (IntegerResult) descriptorResult;
                int value = result.intValue();
                Element element = new Element("Descriptor");
                element.addAttribute(new Attribute("parent", descName));
                element.addAttribute(new Attribute("name", names[0]));
                element.addAttribute(new Attribute("value", Double.toString(value)));
                root.appendChild(element);
            } else if (descriptorResult instanceof DoubleResult) {
                DoubleResult result = (DoubleResult) descriptorResult;
                double value = result.doubleValue();
                Element element = new Element("Descriptor");
                element.addAttribute(new Attribute("parent", descName));
                element.addAttribute(new Attribute("name", names[0]));
                element.addAttribute(new Attribute("value", Double.toString(value)));
                root.appendChild(element);
            } else if (descriptorResult instanceof IntegerArrayResult) {
                IntegerArrayResult result = (IntegerArrayResult) descriptorResult;
                for (int i = 0; i < result.length(); i++) {
                    int value = result.get(i);
                    Element element = new Element("Descriptor");
                    element.addAttribute(new Attribute("parent", descName));
                    element.addAttribute(new Attribute("name", names[i]));
                    element.addAttribute(new Attribute("value", Double.toString(value)));
                    root.appendChild(element);
                }
            } else if (descriptorResult instanceof DoubleArrayResult) {
                DoubleArrayResult result = (DoubleArrayResult) descriptorResult;
                for (int i = 0; i < result.length(); i++) {
                    double value = result.get(i);
                    Element element = new Element("Descriptor");
                    element.addAttribute(new Attribute("parent", descName));
                    element.addAttribute(new Attribute("name", names[i]));
                    element.addAttribute(new Attribute("value", Double.toString(value)));
                    root.appendChild(element);
                }
            }
        }
        Document descriptorDoc = new Document(root);
        return descriptorDoc.toXML();
    }

    /**
     * Get a molecule from its string representation.
     *
     * @param str A string representing the molecule. Can be a SMILES, Base64
     *            encoded SMILES or an SDF. Note that it assumes the string has been
     *            decoded from the URL encoded form
     * @return The molecule
     * @throws CDKException if there was an error during parsing
     */
    public static IAtomContainer getMolecule(String str) throws CDKException {
        if (str == null) throw new CDKException("Null molecule string");

        IAtomContainer atomContainer = null;

        // first try SMILES or base64 encoded SMILES
        if (str.contains("V2000")) { // parse SDF
            MDLV2000Reader reader = new MDLV2000Reader(new StringReader(str));
            ChemFile chemFile = (ChemFile) reader.read(new ChemFile());
            atomContainer = ChemFileManipulator.getAllAtomContainers(chemFile).get(0);
        } else { // some form of SMILES
            SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
            try {
                atomContainer = sp.parseSmiles(str);
            } catch (InvalidSmilesException e) { // OK, maybe Base64?
                byte[] bytes = Base64.decode(str);
                str = new String(bytes);
                try {
                    atomContainer = sp.parseSmiles(str);
                } catch (InvalidSmilesException e1) {
                    throw new CDKException("Invalid SMILES string specified");
                }
            }
        }
        return atomContainer;
    }
}
