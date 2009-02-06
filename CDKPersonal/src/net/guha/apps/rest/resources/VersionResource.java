package net.guha.apps.rest.resources;

import net.guha.apps.rest.Constants;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.*;

/**
 * A resource to provide some versioning information.
 */
public class VersionResource extends Resource {
    public VersionResource(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        return new StringRepresentation(Constants.CDKREST_VERSION + " 'Rajarshi Guha'", MediaType.TEXT_PLAIN);
    }
}
