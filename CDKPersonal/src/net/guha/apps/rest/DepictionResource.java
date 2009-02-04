package net.guha.apps.rest;

import net.guha.apps.rest.representations.ByteRepresentation;
import org.openscience.cdk.exception.CDKException;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;


public class DepictionResource extends Resource {

    String smiles = "";
    int width = 300;
    int height = 300;

    public DepictionResource(Context context, Request request, Response response) {
        super(context, request, response);

        smiles = (String) request.getAttributes().get("smiles");

        if (request.getAttributes().get("width") != null)
            width = Integer.parseInt((String) request.getAttributes().get("width"));

        if (request.getAttributes().get("height") != null)
            height = Integer.parseInt((String) request.getAttributes().get("height"));

        getVariants().add(new Variant(MediaType.IMAGE_JPEG));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = null;
        if (variant.getMediaType().equals(MediaType.IMAGE_JPEG)) {
            StructureDiagram sdg = new StructureDiagram();
            byte[] image;
            try {
                image = sdg.getDiagram(smiles, width, height, 0.9);
            } catch (CDKException e) {
                throw new ResourceException(e);
            }
            assert image != null;
            representation = new ByteRepresentation(image, MediaType.IMAGE_JPEG, image.length);
        }
        return representation;
    }

}
