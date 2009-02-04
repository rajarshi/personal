package net.guha.apps.rest;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Protocol;


public class RESTApplications extends Application {
    public synchronized Restlet createRoot() {
        Router router = new Router(getContext());
        router.attach("/depict/{smiles}/{width}/{height}", DepictionResource.class);
        return router;
    }

    public static void main(String[] args) throws Exception {
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);
        component.getDefaultHost().attach(new RESTApplications());
//        component.getDefaultHost().attach("/trace/{user}", new TracerRestlet());
        component.start();
    }
}
