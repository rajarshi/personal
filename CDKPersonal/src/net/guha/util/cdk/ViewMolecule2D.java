package net.guha.util.cdk;

import org.openscience.cdk.aromaticity.HueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.Renderer2D;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Creates a 2D structure diagram and displays it in a window.
 *
 * @author Rajarshi Guha
 */
public class ViewMolecule2D extends JPanel {
    IAtomContainer molecule;
    JFrame frame;

    Renderer2DModel r2dm;
    Renderer2D renderer;

    int width = 300;
    int height = 300;
    double scale = 0.9;

    static class ApplicationCloser extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            // System.exit(0);
        }
    }

    public ViewMolecule2D(IAtomContainer molecule) {
        this(molecule, 300, 300, 0.9);


    }

    public ViewMolecule2D(IAtomContainer molecule, int width, int height, double scale) {
        this.molecule = molecule;
        this.width = width;
        this.height = height;
        this.scale = scale;
        frame = new JFrame("2D Structure Viewer");
        frame.addWindowListener(new ApplicationCloser());
        frame.setSize(width, height);
    }

    public void paint(Graphics g) {
        super.paint(g);
        renderer.paintMolecule(molecule, (Graphics2D) g, false, true);
    }

    public void draw() {
        molecule = AtomContainerManipulator.removeHydrogens(molecule);
        try {
            HueckelAromaticityDetector.detectAromaticity(molecule);
        } catch (CDKException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        r2dm = new Renderer2DModel();
        renderer = new Renderer2D(r2dm);
        Dimension screenSize = new Dimension(this.width, this.height);
        setPreferredSize(screenSize);
        r2dm.setBackgroundDimension(screenSize); // make sure it is synched with the JPanel size
        setBackground(r2dm.getBackColor());

        try {
            StructureDiagramGenerator sdg = new StructureDiagramGenerator();
            sdg.setMolecule((IMolecule) molecule);
            sdg.generateCoordinates();
            molecule = sdg.getMolecule();

            r2dm.setDrawNumbers(false);
            r2dm.setUseAntiAliasing(true);
            r2dm.setColorAtomsByType(true);
            r2dm.setShowImplicitHydrogens(true);
            r2dm.setShowAromaticity(true);
            r2dm.setShowReactionBoxes(false);
            r2dm.setKekuleStructure(false);

            GeometryTools.translateAllPositive(molecule, r2dm.getRenderingCoordinates());
            GeometryTools.scaleMolecule(molecule, getPreferredSize(), this.scale, r2dm.getRenderingCoordinates());
            GeometryTools.center(molecule, getPreferredSize(), r2dm.getRenderingCoordinates());

        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
        frame.getContentPane().add(this);
        frame.pack();
        frame.setVisible(true);
    }


}

