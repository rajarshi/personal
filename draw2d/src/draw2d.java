/*
 * Rajarshi Guha <rajarshi@presidency.com>
 * 3/4/05
 *
 * 01/19/06 - Thanks to Noel O'Boyle for supplying a patch to update the code
 * 01/19/06 - Updated to produce PDF output if specified
 * 01/20/06 - Applied patch from Egon Willighagen to produce a PDF table of structures
 * 01/22/06 - Updated to allow multiple columns in tabular output
 * 01/22/06 - Applied patch from Egon Willighagen to output properties if present in the SDF file
 * 01/24/06 - Updated to improve the formatting of numbers when the properties are
 *            included in tabular PDF output
 * 02/09/06 - Updated to be in sync with the latest CDK CVS           
 * 03/20/06 - Updated to be in sync with the latest CDK CVS
 * 04/28/06 - Added code to perform explicity aromaticity detection and synced with latest CDK
 * 05/10/06 - Caught an exception, synced with latest CDK
 * 05/18/06 - Updated to show H's for heteroatoms - not configurable from the CLI
 * 06/05/08 - Updated to the latest CDK, removed SVG spport, removed JAI dependency
 * ----------------------------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import com.lowagie.text.*;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.cli.*;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.renderer.old.Renderer2D;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.Map;

class displayStructureAsPDFTable extends displayStructure {
    private PdfPTable table;
    private Document document;
    int ncol = 1;
    boolean props = false;
    boolean doColor = false;
    DecimalFormat decimalFormat;

    public displayStructureAsPDFTable(boolean withH, int width, int height, double scale, int ncol, boolean props, boolean doColor, String odir) {
        this.withH = withH;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.odir = odir;
        this.props = props;
        this.ncol = ncol;
        this.doColor = doColor;

        suffix = ".pdf";
        oformat = "PDF";

        decimalFormat = new DecimalFormat("0.000E00");
        this.frame = new JFrame();

        try {
            File file = new File(odir + "/output" + this.suffix);
            document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));

            float[] widths = new float[ncol];
            for (int i = 0; i < ncol; i += 2) {
                if (props) {
                    widths[i] = 3f;
                    widths[i + 1] = 2f;
                } else {
                    widths[i] = 1.5f;
                    widths[i + 1] = 3f;
                }
            }
            table = new PdfPTable(widths);
            document.open();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private void addTitle(IAtomContainer mol, Phrase phrase) {
        if (mol.getProperty(CDKConstants.TITLE) == null ||
                mol.getProperty(CDKConstants.TITLE).toString().trim().length() == 0) return;
        phrase.add(new Chunk(
                CDKConstants.TITLE + ": ",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD)
        ));
        phrase.add(new Chunk(
                mol.getProperty(CDKConstants.TITLE) + "\n",
                FontFactory.getFont(FontFactory.HELVETICA)
        ));
    }


    private void addProperty(Object propertyLabel, IAtomContainer mol, Phrase phrase) {
        if (mol.getProperty(propertyLabel) == null ||
                mol.getProperty(propertyLabel).toString().trim().length() == 0) return;

        phrase.add(new Chunk(
                propertyLabel + ": ",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD)
        ));
        String value;
        try {
            double tmp = Double.parseDouble(mol.getProperty(propertyLabel).toString());
            value = decimalFormat.format(tmp);
        } catch (NumberFormatException nfe) {
            value = mol.getProperty(propertyLabel).toString();
        }
        phrase.add(new Chunk(
                value + "\n",
                FontFactory.getFont(FontFactory.HELVETICA)
        ));
    }

    public IAtomContainer addHeteroHydrogens(IAtomContainer mol) {
        CDKHydrogenAdder ha = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
        for (int i = 0; i < mol.getAtomCount(); i++) {
            try {
                if (!mol.getAtom(i).getSymbol().equals("C")) {
                    ha.addImplicitHydrogens(mol, mol.getAtom(i));
                }
            } catch (CDKException cdke) {
                cdke.printStackTrace();
            }
        }
        return mol;
    }

    public void drawStructure(IAtomContainer mol, int cnt) {
        mol = addHeteroHydrogens(mol);

        // do aromaticity detection
        try {
            CDKHueckelAromaticityDetector.detectAromaticity(mol);
        } catch (CDKException cdke) {
            cdke.printStackTrace();
        }

        r2dm = new Renderer2DModel();
        renderer = new Renderer2D(r2dm);
        Dimension screenSize = new Dimension(this.width, this.height);
        setPreferredSize(screenSize);
        r2dm.setBackgroundDimension(screenSize); // make sure it is synched with the JPanel size
        setBackground(r2dm.getBackColor());


        try {
            StructureDiagramGenerator sdg = new StructureDiagramGenerator();
            sdg.setMolecule((IMolecule) mol);
            sdg.generateCoordinates();
            this.mol = sdg.getMolecule();

            r2dm.setDrawNumbers(false);
            r2dm.setUseAntiAliasing(true);
            r2dm.setColorAtomsByType(doColor);
            r2dm.setShowExplicitHydrogens(withH);
            r2dm.setShowImplicitHydrogens(true);
            r2dm.setShowAromaticity(true);
            r2dm.setShowReactionBoxes(false);
            r2dm.setKekuleStructure(false);

            GeometryTools.translateAllPositive(this.mol);
            GeometryTools.scaleMolecule(this.mol, getPreferredSize(), this.scale);
            GeometryTools.center(this.mol, getPreferredSize());

        }
        catch (Exception exc) {
            exc.printStackTrace();
        }


        this.frame.getContentPane().add(this);
        this.frame.pack();


        Rectangle pageSize = new Rectangle(this.getSize().width, this.getSize().height);
        try {
            StringBuffer comment = new StringBuffer();

            PdfPCell cell = new PdfPCell();
            Phrase phrase = new Phrase();

            addTitle(mol, phrase);

            if (props) {
                Map<Object, Object> propertyMap = mol.getProperties();
                for (Object key : propertyMap.keySet()) {
                    if (key.equals("AllRings")) continue;
                    if (key.equals("SmallestRings")) continue;
                    if (key.equals(CDKConstants.TITLE)) continue;
                    addProperty(key, mol, phrase);
                }
            }
            cell.addElement(phrase);
            table.addCell(cell);

            Image awtImage = createImage(this.getSize().width, this.getSize().height);
            Graphics snapGraphics = awtImage.getGraphics();
            paint(snapGraphics);

            com.lowagie.text.Image pdfImage = com.lowagie.text.Image.getInstance(awtImage, null);
            pdfImage.setAbsolutePosition(0, 0);
            table.addCell(pdfImage);

        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

    public void paint(Graphics g) {
        super.paint(g);
        renderer.paintMolecule(this.mol, (Graphics2D) g, false, true);
    }

    public void close() throws Exception {
        document.add(table);
        document.close();
    }

}


class displayStructure extends JPanel {
    protected boolean withH = false;
    protected int width = 300;
    protected int height = 300;
    protected double scale = .9;
    protected boolean doColor = false;
    protected String oformat = "PNG";
    protected String odir = "./";
    protected String suffix = ".png";
    protected JFrame frame = null;


    IAtomContainer mol;

    Renderer2DModel r2dm;
    Renderer2D renderer;

    public displayStructure() {
    }

    public IAtomContainer addHeteroHydrogens(IAtomContainer mol) {
        CDKHydrogenAdder ha = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance()) ;
        for (int i = 0; i < mol.getAtomCount(); i++) {
            try {
                if (!mol.getAtom(i).getSymbol().equals("C")) {
                    ha.addImplicitHydrogens(mol,mol.getAtom(i));
                }
            } catch (CDKException cdke) {
                cdke.printStackTrace();
            }
        }
        return mol;
    }

    public displayStructure(boolean withH, int width, int height, double scale, boolean doColor, String oformat, String odir) {
        this.withH = withH;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.oformat = oformat;
        this.odir = odir;
        this.doColor = doColor;

        if (oformat.equalsIgnoreCase("png")) {
            suffix = ".png";
            this.oformat = "PNG";
        } else if (oformat.equalsIgnoreCase("jpeg")) {
            suffix = ".jpg";
            this.oformat = "JPEG";
        } else if (oformat.equalsIgnoreCase("pdf")) {
            suffix = ".pdf";
            this.oformat = "PDF";
        } else if (oformat.equalsIgnoreCase("svg")) {
            suffix = ".svg";
            this.oformat = "SVG";
        }


        this.frame = new JFrame();
    }

    public void drawStructure(IAtomContainer mol, int cnt) {

        // do aromaticity detection
        try {
            CDKHueckelAromaticityDetector.detectAromaticity(mol);
        } catch (CDKException cdke) {
            cdke.printStackTrace();
        }
        mol = addHeteroHydrogens(mol);
        r2dm = new Renderer2DModel();
        renderer = new Renderer2D(r2dm);
        Dimension screenSize = new Dimension(this.width, this.height);
        setPreferredSize(screenSize);
        r2dm.setBackgroundDimension(screenSize); // make sure it is synched with the JPanel size
        setBackground(r2dm.getBackColor());

        try {
            StructureDiagramGenerator sdg = new StructureDiagramGenerator();
            sdg.setMolecule((IMolecule) mol);
            sdg.generateCoordinates();
            this.mol = sdg.getMolecule();

            r2dm.setDrawNumbers(false);
            r2dm.setUseAntiAliasing(true);
            r2dm.setColorAtomsByType(doColor);
            r2dm.setShowAromaticity(true);
            r2dm.setShowAromaticityInCDKStyle(false);
            r2dm.setShowReactionBoxes(false);
            r2dm.setKekuleStructure(false);
            r2dm.setShowExplicitHydrogens(withH);
            r2dm.setShowImplicitHydrogens(true);
            GeometryTools.translateAllPositive(this.mol);
            GeometryTools.scaleMolecule(this.mol, getPreferredSize(), this.scale);
            GeometryTools.center(this.mol, getPreferredSize());
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        this.frame.getContentPane().add(this);
        this.frame.pack();

        String filename;
        if (cnt < 10) filename = this.odir + "/img00" + cnt + this.suffix;
        else if (cnt < 100) filename = this.odir + "/img0" + cnt + this.suffix;
        else filename = this.odir + "/img" + cnt + this.suffix;

        if (oformat.equalsIgnoreCase("png") || oformat.equalsIgnoreCase("jpeg")) {
            BufferedImage img;
            try {
                img = new BufferedImage(this.getSize().width, this.getSize().height, BufferedImage.TYPE_INT_RGB);
//                img = (BufferedImage) createImage(this.getSize().width, this.getSize().height);
                Graphics2D snapGraphics = (Graphics2D) img.getGraphics();
                this.paint(snapGraphics);
                File graphicsFile = new File(filename);
                ImageIO.write(img, oformat, graphicsFile);
            } catch (NullPointerException e) {
                System.out.println(e.toString());
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } else if (oformat.equalsIgnoreCase("pdf")) {
            File file = new File(filename);
            Rectangle pageSize = new Rectangle(this.getSize().width, this.getSize().height);
            Document document = new Document(pageSize);
            try {
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                PdfContentByte cb = writer.getDirectContent();

                Image awtImage = createImage(this.getSize().width, this.getSize().height);
                Graphics snapGraphics = awtImage.getGraphics();
                paint(snapGraphics);

                com.lowagie.text.Image pdfImage = com.lowagie.text.Image.getInstance(awtImage, null);
                pdfImage.setAbsolutePosition(0, 0);
                cb.addImage(pdfImage);

            } catch (DocumentException de) {
                System.err.println(de.getMessage());
            } catch (IOException ioe) {
                System.err.println(ioe.getMessage());
            }
            document.close();
        } else if (oformat.equalsIgnoreCase("svg")) {
	    /*
            try {
                SVGWriter cow = new SVGWriter(new FileWriter(filename));
                if (cow != null) {
                    cow.writeAtomContainer(mol);
                    cow.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (CDKException cdke) {
                cdke.printStackTrace();
            }
	    */
        }
    }

    public void paint(Graphics g) {
        super.paint(g);
        renderer.paintMolecule(this.mol, (Graphics2D) g, false, true);
    }

    public void close() throws Exception {
    }
}

public class draw2d {
    private static boolean withH = false;
    private static int width = 300;
    private static int height = 300;
    private static double scale = 0.9;
    private static String oformat = "PNG";
    private static String outputDirectory = "./";
    private static boolean verbose = false;
    private static boolean tabular = false;
    private static int ncol = 2;
    private static boolean props = false;
    private static boolean doColor = false;

    private void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("draw2d [OPTIONS] sdf_file\n",
                "Draws 2D images from coordinate files. Default output is PNG and input should be an SDF file.\n", options, "");
        System.exit(0);
    }

    private String[] parseCommandLineOptions(String[] args) {

        Options options = new Options();
        options.addOption("c", "scale", true, "Scale factor (default is 0.9)");
        options.addOption("f", "format", true, "Output format (PNG, JPEG, PDF)");
        options.addOption("t", "table", false, "Output structures as a tabular PDF. This option overrides the format option and the output\nfile is called output.pdf");
        options.addOption("n", "ncol", true, "How many molecule columns should tabular output have. The number must be 1 or more");
        options.addOption("p", "props", false, "If present output properties in the PDF table. Default is no");
        options.addOption("h", "help", false, "Give this help page");
        options.addOption("s", "showH", false, "Show explicit hydrogens (default is no)");
        options.addOption("l", "color", false, "Color atoms");
        options.addOption("o", "outputDir", true, "Where to dump images (default is current directory)");
        options.addOption("x", "x", true, "Width of the images (default is 300)");
        options.addOption("y", "y", true, "Height of the images (default is 300)");
        options.addOption("v", "verbose", false, "Verbose output");
        CommandLine line = null;
        try {
            CommandLineParser parser = new PosixParser();
            line = parser.parse(options, args);
        } catch (ParseException exception) {
            System.err.println("Unexpected exception: " + exception.toString());
        }
        if (line.hasOption("l")) {
            this.doColor = true;
        }
        if (line.hasOption("s")) {
            this.withH = true;
        }
        if (line.hasOption("t")) {
            this.tabular = true;
        }
        if (line.hasOption("v")) {
            this.verbose = true;
        }
        if (line.hasOption("p")) {
            this.props = true;
        }
        if (line.hasOption("o")) {
            this.outputDirectory = line.getOptionValue("o");
        }
        if (line.hasOption("x")) {
            this.width = Integer.parseInt(line.getOptionValue("x"));
        }
        if (line.hasOption("y")) {
            this.height = Integer.parseInt(line.getOptionValue("y"));
        }
        if (line.hasOption("c")) {
            this.scale = Double.parseDouble(line.getOptionValue("c"));
        }
        if (line.hasOption("n")) {
            this.ncol = Integer.parseInt(line.getOptionValue("n"));
            if (this.ncol <= 0) {
                printHelp(options);
                System.exit(0);
            }
            this.ncol = 2 * this.ncol;
        }
        if (line.hasOption("f")) {
            this.oformat = line.getOptionValue("f");
            if (!oformat.equalsIgnoreCase("PNG") &&
                    !oformat.equalsIgnoreCase("JPEG") &&
                    !oformat.equalsIgnoreCase("SVG") &&
                    !oformat.equalsIgnoreCase("PDF"))
                oformat = "PNG";
        }


        String[] filesToDraw = line.getArgs();

        if (filesToDraw.length == 0 || line.hasOption("h")) {
            printHelp(options);
        }
        return filesToDraw;
    }

    public static void main(String args[]) {
        draw2d mk = new draw2d();
        String[] filesToDraw = mk.parseCommandLineOptions(args);

        displayStructure ds = null;
        IteratingMDLReader imdlr;

        if (tabular) oformat = "PDF";

        if (verbose) System.out.println("Output format is " + oformat);
        if (tabular) {
            if (verbose) System.out.println("Making a tabular PDF with " + ncol + " columns");
            ds = new displayStructureAsPDFTable(withH, width, height, scale, ncol, props, doColor, outputDirectory);
        }
        try {
            imdlr = new IteratingMDLReader(new BufferedReader(new FileReader(filesToDraw[0])),
                    DefaultChemObjectBuilder.getInstance());
            int cnt = 1;
            while (imdlr.hasNext()) {
                if (verbose) {
                    System.out.println("Processing molecule " + cnt);
                }
                IMolecule m = (IMolecule) imdlr.next();

                // do atom typing
                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(m);
                if (!tabular) {
                    ds = new displayStructure(withH, width, height, scale, doColor, oformat, outputDirectory);
                }
                assert ds != null;
                ds.drawStructure(m, cnt);
                cnt = cnt + 1;
            }
            if (ds != null) ds.close();
            System.exit(0);
        } catch (FileNotFoundException fnf) {
            System.out.println(fnf.toString());
        } catch (Exception fnf) {
            System.out.println(fnf.toString());
            fnf.printStackTrace();
        }
    }
}
