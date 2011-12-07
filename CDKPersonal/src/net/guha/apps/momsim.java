package net.guha.apps;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.openscience.cdk.CDK;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.io.listener.PropertiesListener;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.similarity.DistanceMoment;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Command line to evaluate moment similarity.
 *
 * @author Rajarshi Guha
 */
public class momsim {
    private static final String VERSION = "1.1.2";

    double cutoff = 0.8;
    String outFileName = "mom.txt";
    String queryFileName = null;
    String[] targetFileNames = null;
    int topN = 0;
    boolean generateVectors = false;
    boolean verbose = false;

    public void setCutoff(double cutoff) {
        this.cutoff = cutoff;
    }

    public void setOutFileName(String outFileName) {
        this.outFileName = outFileName;
    }

    public void setQueryFileName(String queryFileName) {
        this.queryFileName = queryFileName;
    }

    public void setTargetFileNames(String[] targetFileNames) {
        this.targetFileNames = targetFileNames;
    }

    public void setGenerateVectors(boolean generateVectors) {
        this.generateVectors = generateVectors;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setTopN(int topN) {
        this.topN = topN;
    }

    class Result implements Comparable {
        String id;
        Float sim;

        Result(String id, float sim) {
            this.id = id;
            this.sim = sim;
        }

        public String getId() {
            return id;
        }

        public Float getSim() {
            return sim;
        }

        @Override
        public String toString() {
            return id + "\t" + sim;
        }

        public int compareTo(Object o) {
            if (o instanceof Result) {
                Result r = (Result)o;
                return sim.compareTo(r.getSim());
            }
            return 0;
        }
    }


    public static void main(String[] args) throws CDKException, IOException {

        momsim obj = new momsim();

        Options options = new Options();
        options.addOption("h", false, "Help");
        options.addOption("v", false, "Verbose output");
        options.addOption("g", false, "Generate moments");
        options.addOption("o", true, "Output file (default is mom.txt)");
        options.addOption("q", true, "Query file (SDF)");
        options.addOption("c", true, "Similarity cutoff [0-1]. Default is 0.8");
        options.addOption("t", true, "Keep top N most similar compounds only. N >= 1");

        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                usage(options);
                System.exit(-1);
            }
            if (cmd.hasOption("v")) obj.setVerbose(true);
            if (cmd.hasOption("g")) obj.setGenerateVectors(true);
            if (cmd.hasOption("q")) obj.setQueryFileName(cmd.getOptionValue("q"));
            if (cmd.hasOption("o")) obj.setOutFileName(cmd.getOptionValue("o"));
            if (cmd.hasOption("c")) obj.setCutoff(Double.parseDouble(cmd.getOptionValue("c")));
//            if (cmd.hasOption("t")) obj.setTopN(Integer.parseInt(cmd.getOptionValue("t")));

            String[] remainder = cmd.getArgs();
            if (remainder.length == 0) {
                usage(options);
                System.exit(-1);
            }
            obj.setTargetFileNames(remainder);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        obj.run();
    }

    private String join(float[] x, String delim) {
        if (delim == null) delim = "";
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < x.length; i++) {
            if (Float.isNaN(x[i])) buffer.append("NA");
            else buffer.append(x[i]);
            if (i != x.length - 1) buffer.append(delim);
        }
        return buffer.toString();
    }


    private boolean has3D(IAtomContainer mol) {
        for (IAtom atom : mol.atoms()) {
            if (atom.getPoint3d() == null) return false;
        }
        return true;
    }


    private void run() throws IOException, CDKException {

        if (!generateVectors && queryFileName == null) {
            System.err.println("ERROR: If you're not generating vectors, then you must specify a query structure");
            System.exit(-1);
        }
        if (generateVectors && targetFileNames == null) {
            System.err.println("ERROR: Must specify the target structures");
            System.exit(-1);
        }

        // read in query structure if we need to
        IAtomContainer query = null;
        if (queryFileName != null) {
            MDLV2000Reader reader = new MDLV2000Reader(new FileInputStream(queryFileName));
            Properties qprop = new Properties();
            qprop.setProperty("ForceReadAs3DCoordinates", "true");
            PropertiesListener listener = new PropertiesListener(qprop);
            reader.addChemObjectIOListener(listener);
            reader.customizeJob();

            query = NoNotificationChemObjectBuilder.getInstance().newInstance(IAtomContainer.class);
            query = reader.read(query);
            if (verbose)
                System.out.println("INFO read query structure [" + query.getProperty(CDKConstants.TITLE) + "]");
            if (!has3D(query)) {
                System.err.println("ERROR: Query structure must have 3D coordinates");
                System.exit(-1);
            }
            if (query.getAtomCount() == 1) {
                System.err.println("ERROR: Can't work with a single atom query");
                System.exit(-1);
            }
            reader.close();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName));

        for (String targetFileName : targetFileNames) {
            if (verbose) System.out.println("Processing " + targetFileName);

            IteratingMDLReader ireader = null;
            try {
                ireader = new IteratingMDLReader(new FileReader(targetFileName), NoNotificationChemObjectBuilder.getInstance());
            } catch (FileNotFoundException e) {
                System.err.println("ERROR: Couldn't open " + targetFileName);
                continue;
            }
            Properties prop = new Properties();
            prop.setProperty("ForceReadAs3DCoordinates", "true");
            PropertiesListener listener = new PropertiesListener(prop);
            ireader.addChemObjectIOListener(listener);
            ireader.customizeJob();

            int nmol = 0;
            long start, end;

            if (generateVectors) {
                start = System.currentTimeMillis();
                while (ireader.hasNext()) {
                    IAtomContainer target = (IAtomContainer) ireader.next();
                    float[] moments;
                    nmol++;
                    if (!has3D(target) || target.getAtomCount() == 1) {
                        System.err.println("\nERROR: " + target.getProperty(CDKConstants.TITLE) + " had no 3D coordinates or else has a single atom. Using NAs");
                        moments = new float[12];
                        for (int i = 0; i < 12; i++) moments[i] = Float.NaN;
                    } else {
                        moments = DistanceMoment.generateMoments(target);
                    }
                    String row = target.getProperty(CDKConstants.TITLE) + "," + join(moments, ",");
                    writer.write(row + "\n");
                    if (verbose && nmol % 100 == 0) System.out.print("\rProcessed " + nmol + " molecules");
                }

                end = System.currentTimeMillis();
                if (verbose)
                    System.out.println("\rGenerated moment vectors for " + nmol + " molecules in " + (float) (end - start) / 1000 + " sec [" + (float) nmol / ((end - start) / 1000) + " mol/s]");
            } else {
                // we are to perform a similarity calculation. We should already have gotten a query structure
                float[] queryMoments = DistanceMoment.generateMoments(query);
                int nsel = 0;
                start = System.currentTimeMillis();
                while (ireader.hasNext()) {
                    IAtomContainer target = (IAtomContainer) ireader.next();
                    nmol++;
                    if (!has3D(target) || target.getAtomCount() == 1) {
                        System.err.println("\nERROR: " + target.getProperty(CDKConstants.TITLE) + " had no 3D coordinates or else a single atom. Skipping");
                        continue;
                    }
                    float[] targetMoments = DistanceMoment.generateMoments(target);
                    float sum = 0;
                    for (int i = 0; i < queryMoments.length; i++) {
                        sum += Math.abs(queryMoments[i] - targetMoments[i]);
                    }
                    float sim = (float) (1.0 / (1.0 + sum / 12.0));
                    if (sim >= cutoff) {
                        writer.write(target.getProperty(CDKConstants.TITLE) + "\t" + sim + "\n");
                        nsel++;
                    }
                    if (verbose && nmol % 100 == 0)
                        System.out.print("\rProcessed " + nmol + " molecules and found " + nsel + " similar");
                }
                end = System.currentTimeMillis();
                if (verbose)
                    System.out.println("\rProcessed " + nmol + " molecules and found " + nsel + " similar in " + (float) (end - start) / (1000) + " sec");
            }
            ireader.close();
        }
        writer.close();

    }


    private static void usage(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("momsim [OPTIONS] inputfile inputfile ...\n", options);
        System.out.println("\ninputfile should be an SD file containing target structures.\nIdeally each molecule should have a title");
        System.out.println("\nmomsim v" + VERSION + " (CDK " + CDK.getVersion() + ") Rajarshi Guha <rajarshi.guha@gmail.com>\n");
        System.exit(-1);

    }
}
