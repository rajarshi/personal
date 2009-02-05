package net.guha.apps.rest;

import net.guha.apps.rest.resources.DepictionResource;
import net.guha.apps.rest.resources.SubstructureSearchResource;
import net.guha.apps.rest.resources.TPSAResource;
import org.restlet.*;
import org.restlet.data.Protocol;


public class CDKServices extends Application {
    public synchronized Restlet createRoot() {
        Router router = new Router(getContext());

        // setup depiction service
        router.attach("/depict/{width}/{height}/{smiles}", DepictionResource.class);
        router.attach("/depict/{smiles}", new Redirector(getContext(), "/depict/200/200/{smiles}", Redirector.MODE_CLIENT_PERMANENT));

        router.attach("/descriptors/tpsa/{smiles}", TPSAResource.class);
        router.attach("/descriptors/xlogp/{smiles}", TPSAResource.class);

        router.attach("/substruct/{target}/{query}", SubstructureSearchResource.class);
        return router;
    }

    public static void main(String[] args) throws Exception {
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, "rguha.ath.cx", 8182);
        component.getDefaultHost().attach(new CDKServices());
//        component.getDefaultHost().attach("/trace/{user}", new TracerRestlet());
        component.start();
    }
}
