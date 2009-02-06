package net.guha.apps.rest.resources;

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
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.*;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;


public class DescriptorsResource extends Resource {

    String smiles;

    public DescriptorsResource(Context context, Request request, Response response) {
        super(context, request, response);
        smiles = (String) request.getAttributes().get("smiles");
        if (smiles != null) smiles = Reference.decode(smiles);
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.APPLICATION_XML));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = null;
        String result = "";
        try {
            String[] names = getAvailableDescriptorNames("all");
            representation = new StringRepresentation(namesToXml(names), MediaType.TEXT_XML);
        } catch (CDKException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return representation;
    }

    public String namesToXml(String[] names) {
        Element root = new Element("descriptor-list");
        for (String s: names) {
            Element element = new Element("descriptor-ref");
            element.addAttribute(new Attribute("href", s));
            root.appendChild(element);
        }
        return root.toXML();
    }
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
    public String[] getAvailableDescriptorNames(String type) throws CDKException, MalformedURLException {

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

    public String evaluateDescriptors(String[] classNames, String molecule) throws CDKException, ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {
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

}