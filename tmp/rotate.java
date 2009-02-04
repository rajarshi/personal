/*
 * Rajarshi Guha <rajarshi@presidency.com>
 * 18/10/2005
 */

import jama.Matrix;

import java.util.Random;
import java.util.Vector;

import java.io.Reader;
import java.io.Writer;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;

import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.openscience.cdk.interfaces.Atom;
import org.openscience.cdk.interfaces.AtomContainer;
import org.openscience.cdk.interfaces.ChemFile;
import org.openscience.cdk.interfaces.ChemObject;
import org.openscience.cdk.interfaces.Molecule;

import org.openscience.cdk.io.ChemObjectIO;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.ChemObjectReader;
import org.openscience.cdk.io.ChemObjectWriter;

import org.openscience.cdk.io.CDKSourceCodeWriter;
import org.openscience.cdk.io.CMLWriter;
import org.openscience.cdk.io.HINWriter;
import org.openscience.cdk.io.MDLWriter;
import org.openscience.cdk.io.PDBWriter;
import org.openscience.cdk.io.SMILESWriter;
import org.openscience.cdk.io.SVGWriter;
import org.openscience.cdk.io.ShelXWriter;
import org.openscience.cdk.io.XYZWriter;
import org.openscience.cdk.io.program.GaussianInputWriter;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class rotate {

    private static int whichAxis = 0;
    private static double angle = 0;
    private static String ofile = "";
    private static String ifile = "";
    private static String format = "";
    
    private static Vector loadMoleculesSimple(String[] args, boolean withH) {
        Vector v = new Vector();
        try {
            int i = 0;
            int j = 0;

            // load the molecule and generate 2D coordinates
            for (i = 0; i < args.length; i++) {
                File input = new File(args[i]);
                ChemObjectReader reader = new ReaderFactory().createReader(new FileReader(input));
                ChemFile content = (ChemFile)reader.read((ChemObject)new org.openscience.cdk.ChemFile());
                format = reader.getFormat().getFormatName();
                AtomContainer[] c = ChemFileManipulator.getAllAtomContainers(content);

                // we should do this loop in case we have files
                // that contain multiple molecules
                for (j = 0; j < c.length; j++) {
                    v.add(c[j]);
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return v;
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("rotate [OPTIONS] filename\n", options);
        System.out.println(
                "\nNote that if a different output file name is specified"+
                " the new file will be of the same format as the old file");
        
        System.exit(0);
    }

    public static void parseCLI(String[] args) {
        Options options = new Options();
        options.addOption("h", "help", false, "give this help page");
        options.addOption("a", "axis", true, "which axis to rotate about (X,Y or Z. Default is X)");
        options.addOption("t", "theta", true, "angle (in degrees, default is 0)");
        options.addOption("o", "output", true, "output file name (default is the input file)");
        CommandLine line = null;
        try {
            CommandLineParser parser = new PosixParser();
            line = parser.parse(options, args);
        } catch (ParseException exception) {
            System.err.println("Unexpected exception: " + exception.toString());
        }
        if (line.hasOption("a")) {
            String axis = line.getOptionValue("a");
            if (axis.equals("X") || axis.equals("x")) whichAxis = 0;
            if (axis.equals("Y") || axis.equals("y")) whichAxis = 1;
            if (axis.equals("Z") || axis.equals("z")) whichAxis = 2;
        }
        if (line.hasOption("t")) {
            angle = Double.parseDouble(line.getOptionValue("t"));
            angle = angle * Math.PI / 180.0;
        }
        if (line.hasOption("o")) {
            ofile = line.getOptionValue("o");
        }
        
        String[] filesToConvert = line.getArgs();
        if (filesToConvert.length == 0 || line.hasOption("h")) {
            printHelp(options);
        }
        ifile = filesToConvert[0];
        if (ofile.equals("")) ofile = ifile;
        
    }

    public static void main(String[] args) {

        parseCLI(args);

        Vector v = loadMoleculesSimple(new String[] {ifile}, true);

        AtomContainer ac = (AtomContainer)v.get(0);
        int natom = ac.getAtomCount();
        double[][] coords = new double[3][natom];
        Atom[] atoms = ac.getAtoms();
        for (int i = 0; i < natom; i++) {
            coords[0][i] = atoms[i].getX3d();
            coords[1][i] = atoms[i].getY3d();
            coords[2][i] = atoms[i].getZ3d();
        }

        Matrix r = new Matrix(3,3);

        double c = Math.cos(angle);
        double s = Math.sin(angle);

        switch(whichAxis) {
            case 0: // X axis
                r.set(0,0,1);
                r.set(1,1,c);
                r.set(1,2,s);
                r.set(2,1,-s);
                r.set(2,2,c);
                break;
            case 1: // Y axis
                r.set(0,0,c);
                r.set(0,2,-s);
                r.set(1,1,1);
                r.set(2,0,s);
                r.set(2,2,c);
                break;
            case 2: // Z axis
                r.set(0,0,c);
                r.set(0,1,s);
                r.set(1,0,-s);
                r.set(1,1,c);
                r.set(2,2,1);
                break;
        }

        // do the rotation
        Matrix mc = new Matrix(coords);
        mc = r.times(mc);

        // put the coordinates back
        for (int j = 0; j < natom; j++) {
            ac.getAtomAt(j).setX3d( mc.get(0, j) );
            ac.getAtomAt(j).setY3d( mc.get(1, j) );
            ac.getAtomAt(j).setZ3d( mc.get(2, j) );
        }

        try {
            FileWriter w = new FileWriter(new File(ofile));
            ChemObjectWriter cow = getChemObjectWriter(format, w);
            cow.write(ac);
            cow.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }


    private static ChemObjectWriter getChemObjectWriter(String format, Writer fileWriter) {
        ChemObjectWriter writer = null;
        try {
            if (format.indexOf("CML") != -1) {
                writer = new CMLWriter(fileWriter);
            } else if (format.indexOf("MOL") != -1) {
                writer = new MDLWriter(fileWriter);
            } else if (format.indexOf("SMI") != -1) {
                writer = new SMILESWriter(fileWriter);
            } else if (format.indexOf("SHELX") != -1) {
                writer = new ShelXWriter(fileWriter);
            } else if (format.indexOf("SVG") != -1) {
                writer = new SVGWriter(fileWriter);
            } else if (format.indexOf("XYZ") != -1) {
                writer = new XYZWriter(fileWriter);
            } else if (format.indexOf("PDB") != -1) {
                writer = new PDBWriter(fileWriter);
            } else if (format.indexOf("GIN") != -1) {
                writer = new GaussianInputWriter(fileWriter);
            } else if (format.indexOf("CDK") != -1) {
                writer = new CDKSourceCodeWriter(fileWriter);
            } else if (format.indexOf("HIN") != -1) {
                writer = new HINWriter(fileWriter);
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        return writer;
    }

}









