package net.guha.apps.rest.resources;

import net.guha.apps.rest.Utils;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
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


public class MWResource extends Resource {

    String smiles = "";

    public MWResource(Context context, Request request, Response response) {
        super(context, request, response);
        smiles = Reference.decode((String) request.getAttributes().get("smiles"));
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
                AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol);
                result = String.valueOf(MolecularFormulaManipulator.getNaturalExactMass(MolecularFormulaManipulator.getMolecularFormula(mol)));
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