package net.guha.apps.rest.resources;

import net.guha.apps.rest.StructureDiagram;
import net.guha.apps.rest.Utils;
import net.guha.apps.rest.representations.ByteRepresentation;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.*;

import java.awt.*;


public class DepictionResource extends Resource {

    String smiles = "";
    int width = 300;
    int height = 300;

    MediaType contentType = MediaType.IMAGE_PNG;

    public DepictionResource(Context context, Request request, Response response) {
        super(context, request, response);

        smiles = (String) request.getAttributes().get("smiles");
        if (smiles != null) smiles = Reference.decode(smiles);

        Object width = request.getAttributes().get("width");
        Object height = request.getAttributes().get("height");
        if (width != null) this.width = Integer.parseInt((String) width);
        if (height != null) this.height = Integer.parseInt((String) height);
        getVariants().add(new Variant(contentType));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = null;
        if (variant.getMediaType().equals(contentType)) {
            if (GraphicsEnvironment.isHeadless()) throw new ResourceException(new CDKException("Running headless!"));
            if (smiles == null) throw new ResourceException(new CDKException("No SMILES specified?"));
            StructureDiagram sdg = new StructureDiagram();
            byte[] image;
            try {
                image = sdg.getDiagram(Utils.getMolecule(smiles), null, width, height, 0.9);
            } catch (CDKException e) {
                throw new ResourceException(e);
            }
            assert image != null;
            representation = new ByteRepresentation(image, contentType, image.length);
        }
        return representation;
    }

    public boolean allowPost() {
        return true;
    }

    public void setModifiable(boolean b) {
        super.setModifiable(false);
    }

    public void acceptRepresentation(Representation representation) throws ResourceException {
        if (representation.getMediaType().equals(MediaType.APPLICATION_WWW_FORM)) {
            Form form = new Form(representation);
            String str = form.getFirstValue("molecule");
            String width = form.getFirstValue("width");
            String height = form.getFirstValue("height");
            if (str == null)
                throw new ResourceException(new CDKException("Must specify a molecule"));

            int w = 200;
            int h = 200;
            if (width != null) w = Integer.parseInt(width);
            if (height != null) h = Integer.parseInt(height);

            // get the molecule
            IAtomContainer atomContainer;
            try {
                atomContainer = Utils.getMolecule(str);
            } catch (CDKException e) {
                throw new ResourceException(e);
            }

            // draw the molecule
            StructureDiagram sdg = new StructureDiagram();
            byte[] image;
            try {
                image = sdg.getDiagram(atomContainer, null, w, h, 0.9);
            } catch (CDKException e) {
                throw new ResourceException(e);
            }
            assert image != null;
            representation = new ByteRepresentation(image, contentType, image.length);
            getResponse().setStatus(Status.SUCCESS_OK);
            getResponse().setEntity(representation);
        } else

        {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
    }

}
