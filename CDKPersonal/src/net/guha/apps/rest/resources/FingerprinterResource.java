package net.guha.apps.rest.resources;

import net.guha.apps.rest.Utils;
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
import org.restlet.data.*;
import org.restlet.resource.*;

import java.util.BitSet;


public class FingerprinterResource extends Resource {

    String smiles = "";
    String type;

    public FingerprinterResource(Context context, Request request, Response response) {
        super(context, request, response);
        smiles = (String) request.getAttributes().get("smiles");
        type = (String) request.getAttributes().get("type");
        if (smiles != null) smiles = Reference.decode(smiles);
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = null;
        String result = "";
        if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
            IMolecule mol;
            try {
                mol = (IMolecule) Utils.getMolecule(smiles);
                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
                CDKHueckelAromaticityDetector.detectAromaticity(mol);
                IFingerprinter fp;
                if (type.equals("std")) fp = new Fingerprinter();
                else if (type.equals("maccs")) fp = new MACCSFingerprinter();
                else if (type.equals("estate")) fp = new EStateFingerprinter();
                else throw new CDKException("Invalid fingerprint type was specified");
                BitSet bitset = fp.getFingerprint(mol);
                result = bitSetToBinaryString(bitset, fp.getSize());
            } catch (InvalidSmilesException e) {
                throw new ResourceException(e);
            } catch (CDKException e) {
                throw new ResourceException(e);
            }
            representation = new StringRepresentation(result, MediaType.TEXT_PLAIN);
        }
        return representation;
    }

    public boolean allowPost() {
        return true;
    }

    public void setModifiable(boolean b) {
        super.setModifiable(false);
    }

    // handle a POST request
    public void acceptRepresentation(Representation representation) throws ResourceException {
        if (representation.getMediaType().equals(MediaType.APPLICATION_WWW_FORM)) {
            Form form = new Form(representation);
            String type = form.getFirstValue("type");
            String tmp = form.getFirstValue("smiles");

            if (tmp == null || type == null)
                throw new ResourceException(new CDKException("No form elements specified"));

            String[] smiles = tmp.split(",");

            IFingerprinter fp;
            if (type.equals("std")) fp = new Fingerprinter();
            else if (type.equals("maccs")) fp = new MACCSFingerprinter();
            else if (type.equals("estate")) fp = new EStateFingerprinter();
            else throw new ResourceException(new CDKException("Invalid fingerprint type was specified"));

            SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
            IMolecule mol;
            StringBuffer sb = new StringBuffer();
            String result = null;
            try {
                for (String s : smiles) {
                    mol = sp.parseSmiles(s.trim());
                    AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
                    CDKHueckelAromaticityDetector.detectAromaticity(mol);
                    BitSet bitset = fp.getFingerprint(mol);
                    sb.append(bitSetToBinaryString(bitset, fp.getSize()) + "\n");
                }
                sb.deleteCharAt(sb.length() - 1);
            } catch (CDKException e) {
                throw new ResourceException(e);
            }
            getResponse().setStatus(Status.SUCCESS_OK);
            getResponse().setEntity(new StringRepresentation(sb.toString(), MediaType.TEXT_PLAIN));
        } else {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
    }

    private String bitSetToBinaryString(BitSet b, int length) {
        StringBuffer sb = new StringBuffer(length);
        for (int i = 0; i < length; i++) sb.append(0);
        for (int i = b.nextSetBit(0); i >= 0; i = b.nextSetBit(i + 1)) {
            sb.setCharAt(i, '1');
        }
        return sb.toString();
    }

}