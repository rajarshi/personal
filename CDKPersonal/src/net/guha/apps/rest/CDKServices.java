package net.guha.apps.rest;

import net.guha.apps.rest.resources.*;
import org.apache.commons.cli.*;
import org.restlet.*;
import org.restlet.data.Protocol;
import org.restlet.service.LogService;

import java.util.logging.FileHandler;
import java.util.logging.Logger;


public class CDKServices extends Application {

    private static Logger logger = Logger.getLogger("net.guha");

    public synchronized Restlet createRoot() {
        Router router = new Router(getContext());

        // setup depiction service
        router.attach("/cdk/depict/{width}/{height}/{smiles}", DepictionResource.class);
        router.attach("/cdk/depict/{smiles}", new Redirector(getContext(), "/cdk/depict/200/200/{smiles}", Redirector.MODE_CLIENT_PERMANENT));

        router.attach("/cdk/descriptors/tpsa/{smiles}", TPSAResource.class);
        router.attach("/cdk/descriptors/xlogp/{smiles}", TPSAResource.class);

        // the second route allows for POST requests where we match
        // multiple SMILES
        router.attach("/cdk/substruct/{target}/{query}", SubstructureSearchResource.class);
        router.attach("/cdk/substruct", SubstructureSearchResource.class);
        
        // fingerprints, using default values. If no type is specified
        // use the standard fingerprinterit 
        router.attach("/cdk/fingerprint/{type}/{smiles}", FingerprinterResource.class);
        router.attach("/cdk/fingerprint/{smiles}", new Redirector(getContext(), "/cdk/fingerprint/std/{smiles}", Redirector.MODE_CLIENT_PERMANENT));

        // molecular weight and formulae
        router.attach("/cdk/mw/{smiles}", MWResource.class);
        router.attach("/cdk/mf/{smiles}", MFResource.class);
        return router;
    }

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("h", "help", false, "Print this message");
        options.addOption("s", "server", true, "Host name for the web server. Default is localhost");
        options.addOption("p", "port", true, "Port number. Default is 8182");
        options.addOption("l", "log", true, "Name for log file. If not specified " +
                " log messages are sent to STDOUT");

        CommandLine line = null;
        try {
            CommandLineParser parser = new PosixParser();
            line = parser.parse(options, args);
        } catch (ParseException exception) {
            System.err.println("Unexpected exception: " + exception.toString());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("\njava -jar cdkrest.jar",
                    "CDK REST services (Rajarshi Guha <rajarshi.guha@gmail.com>)\n",
                    options, "");
            System.exit(-1);
        }
        if (line.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar cdkrest.jar [OPTIONS]", options);
            System.exit(-1);
        }
        String server = "localhost";
        int port = 8182;
        String logfilename = null;

        if (line.hasOption("s")) server = line.getOptionValue("s");
        if (line.hasOption("p")) port = Integer.parseInt(line.getOptionValue("p"));
        if (line.hasOption("l")) logfilename = line.getOptionValue("l");

        Component component = new Component();
        component.getServers().add(Protocol.HTTP, server, port);
        component.getDefaultHost().attach(new CDKServices());


        LogService logService = component.getLogService();
        logService.setEnabled(true);
        logService.setLoggerName("net.guha");

        if (logfilename != null) {
            FileHandler fh = new FileHandler(logfilename);
            logger.setUseParentHandlers(false);
            logger.addHandler(fh);
        }

        component.start();
    }
}
