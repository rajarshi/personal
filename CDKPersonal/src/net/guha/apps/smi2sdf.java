package net.guha.apps;

import org.apache.commons.cli.*;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.MDLWriter;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.io.File;
import java.io.FileWriter;

public class smi2sdf {

    String outputFileName = "output.sdf";
    boolean withH = false;
    boolean verbose = true;

    private void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("smi2sdf [OPTIONS] SMILES\n",
                "Converts a SMILE string to a 2D SDF", options, "");
        System.exit(0);
    }

    private String[] parseCommandLineOptions(String[] args) {

        Options options = new Options();
        options.addOption("h", "help", false, "Give this help page");
        options.addOption("o", "output", true, "Output file name (default is output.sdf)");
        options.addOption("w", "withH", false, "Add hydrogens (default is not to add)");
        options.addOption("v", "verbose", false, "Verbose output");
        CommandLine line = null;

        try {
            CommandLineParser parser = new PosixParser();
            line = parser.parse(options, args);
        } catch (ParseException exception) {
            System.err.println("Unexpected exception: " + exception.toString());
        }
        if (line.hasOption("w")) {
            this.withH = true;
        }
        if (line.hasOption("v")) {
            this.verbose = true;
        }
        if (line.hasOption("o")) {
            this.outputFileName = line.getOptionValue("o");
        }

        String[] smi = line.getArgs();

        if (smi.length == 0 || line.hasOption("h")) {
            printHelp(options);
        }
        return smi;
    }

    private void doConversion(String smi) {
        IMolecule mol = null;
        try {
            SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
            mol = sp.parseSmiles(smi);
        } catch (InvalidSmilesException ise) {
            System.out.println(ise.toString());
        }

        try {
            if (withH) {
                CDKHydrogenAdder ha = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
                ha.addImplicitHydrogens(mol);
                AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol);
            }
        } catch (CDKException e) {
            e.printStackTrace();
        }

        // get the 2D coords
        try {
            StructureDiagramGenerator sdg = new StructureDiagramGenerator();
            sdg.setMolecule(mol);
            sdg.generateCoordinates();
            mol = sdg.getMolecule();
        }
        catch(Exception exc) {
            exc.printStackTrace();
        }

        // write to disk
        try {
            FileWriter w = new FileWriter(new File(outputFileName));
            MDLWriter mw = new MDLWriter(w);
            mw.write(mol);
            mw.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public static void main(String args[]) {
        smi2sdf mk = new smi2sdf();
        String[] smi = mk.parseCommandLineOptions(args);

        if (smi.length > 1) {
            System.out.println("Only 1 SMILES string for now!");
            System.exit(0);
        }
        mk.doConversion(smi[0]);
    }

}
