import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.io.*;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.HydrogenAdder;

import org.openscience.cdk.exception.*;


import java.util.*;
import java.io.*;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

public class smi2cml {

    String outputFileName = "output.cml";
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
            SmilesParser sp = new SmilesParser();
            mol = sp.parseSmiles(smi);
        } catch (InvalidSmilesException ise) {
            System.out.println(ise.toString());
        }

        try {
            if (withH) {
                HydrogenAdder ha = new HydrogenAdder();
                ha.addExplicitHydrogensToSatisfyValency(mol);
            } 
        }catch (IOException e) {
            e.printStackTrace();
        }catch(ClassNotFoundException e) {
            e.printStackTrace();
        }catch(CDKException e) {
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
            CMLWriter mw = new CMLWriter(w);
            mw.write(mol);
            mw.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }        

    public static void main(String args[]) {
        smi2cml mk = new smi2cml();
        String[] smi = mk.parseCommandLineOptions(args);

        if (smi.length > 1) {
            System.out.println("Only 1 SMILES string for now!");
            System.exit(0);
        }
        mk.doConversion(smi[0]);
    }

}

