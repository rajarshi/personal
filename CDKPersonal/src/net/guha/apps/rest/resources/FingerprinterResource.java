package net.guha.apps.rest.resources;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.fingerprint.EStateFingerprinter;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.fingerprint.IFingerprinter;
import org.openscience.cdk.fingerprint.MACCSFingerprinter;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.*;


public class FingerprinterResource extends Resource {

    String smiles = "";
    String type;

    public FingerprinterResource(Context context, Request request, Response response) {
        super(context, request, response);
        smiles = (String) request.getAttributes().get("smiles");
        type = (String) request.getAttributes().get("type");
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = null;
        String result = "";
        if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {

            SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
            IMolecule mol = null;
            try {
                mol = sp.parseSmiles(smiles);
                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
                CDKHueckelAromaticityDetector.detectAromaticity(mol);
                IFingerprinter fp;
                if (type.equals("std")) fp = new Fingerprinter();
                else if (type.equals("maccs")) fp = new MACCSFingerprinter();
                else if (type.equals("estate")) fp = new EStateFingerprinter();
                else throw new CDKException("Invalid fingerprint type was specified");
                result = fp.getFingerprint(mol).toString();
            } catch (InvalidSmilesException e) {
                throw new ResourceException(e);
            } catch (CDKException e) {
                throw new ResourceException(e);
            }
            representation = new StringRepresentation(result, MediaType.TEXT_PLAIN);
        }
        return representation;
    }

}