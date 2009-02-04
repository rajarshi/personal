package net.guha.apps.rest;

import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;


public class TracerRestlet extends Restlet {
    public void handle(Request request, Response response) {
        // Print the requested URI path
        String message = "Resource URI  : " + request.getResourceRef()
                + '\n' + "Root URI      : " + request.getRootRef()
                + '\n' + "Routed part   : "
                + request.getResourceRef().getBaseRef() + '\n'
                + "Remaining part: "
                + request.getResourceRef().getRemainingPart()
                + "User: "
                + request.getAttributes().get("user");

        response.setEntity(message, MediaType.TEXT_PLAIN);
    }
}
