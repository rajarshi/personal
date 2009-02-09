package net.guha.apps.rest.resources;

import net.guha.apps.rest.Utils;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor;
import org.openscience.cdk.qsar.result.DoubleResult;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.*;


public class XLOGPResource extends Resource {

    String smiles = "";

    public XLOGPResource(Context context, Request request, Response response) {
        super(context, request, response);
        smiles = (String) request.getAttributes().get("smiles");
        if (smiles != null) smiles = Reference.decode(smiles);
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = null;
        double result = -1.0;
        if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {
            IMolecule mol;
            try {
                mol = (IMolecule) Utils.getMolecule(smiles);
                CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
                hAdder.addImplicitHydrogens(mol);
                AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol);
                XLogPDescriptor xlogp = new XLogPDescriptor();
                xlogp.setParameters(new Object[]{Boolean.TRUE, Boolean.TRUE});
                result = ((DoubleResult) xlogp.calculate(mol).getValue()).doubleValue();
            } catch (InvalidSmilesException e) {
                throw new ResourceException(e);
            } catch (CDKException e) {
                throw new ResourceException(e);
            }
            representation = new StringRepresentation(Double.toString(result), MediaType.TEXT_PLAIN);
        }
        return representation;
    }

}