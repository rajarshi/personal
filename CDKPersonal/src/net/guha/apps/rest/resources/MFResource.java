package net.guha.apps.rest.resources;

import net.guha.apps.rest.Utils;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.*;


public class MFResource extends Resource {

    String smiles = "";

    public MFResource(Context context, Request request, Response response) {
        super(context, request, response);
        smiles = Reference.decode((String) request.getAttributes().get("smiles"));
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = null;
        String result = "";
        
        IMolecule mol;
        try {
            mol = (IMolecule) Utils.getMolecule(smiles);
            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
            AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol);
        } catch (InvalidSmilesException e) {
            throw new ResourceException(e);
        } catch (CDKException e) {
            throw new ResourceException(e);
        }

        if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
            result = MolecularFormulaManipulator.getString(MolecularFormulaManipulator.getMolecularFormula(mol));
            representation = new StringRepresentation(result, MediaType.TEXT_PLAIN);
        } else if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
            result = MolecularFormulaManipulator.getHTML(MolecularFormulaManipulator.getMolecularFormula(mol));
            representation = new StringRepresentation(result, MediaType.TEXT_HTML);
        }
        return representation;
    }

}