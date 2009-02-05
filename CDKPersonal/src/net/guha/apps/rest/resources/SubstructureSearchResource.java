package net.guha.apps.rest.resources;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.*;

/**
 * Perform substructure searches.
 *
 * The class currently only supports matching a single query against
 * a single target using a GET request. Also the service only
 * determines whether the target contains the query, return "true" if
 * present and "false" otherwise.
 */
public class SubstructureSearchResource extends Resource {

    String target;
    String query;

    public SubstructureSearchResource(Context context, Request request, Response response) {
        super(context, request, response);
        target = (String) request.getAttributes().get("target");
        query = (String) request.getAttributes().get("query");
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = null;
        boolean matches = false;
        if (variant.getMediaType().equals(MediaType.TEXT_PLAIN)) {

            SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
            try {
                SMARTSQueryTool sqt = new SMARTSQueryTool("C");
                sqt.setSmarts(Reference.decode(query));
                IMolecule mol = sp.parseSmiles(target);
                matches = sqt.matches(mol);
            } catch (CDKException e) {
                throw new ResourceException(e);
            }
            if (matches)
                representation = new StringRepresentation("true", MediaType.TEXT_PLAIN);
            else representation = new StringRepresentation("false", MediaType.TEXT_PLAIN);
        }
        return representation;
    }

}