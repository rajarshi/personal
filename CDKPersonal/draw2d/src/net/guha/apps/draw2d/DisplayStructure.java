package net.guha.apps.draw2d;

import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.old.Renderer2D;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import javax.swing.*;
import java.awt.*;

/**
 * @author Rajarshi Guha
 */
public class DisplayStructure extends JPanel {
    JFrame frame;
    IAtomContainer mol;

    Renderer2DModel r2dm;
    Renderer2D renderer;

    public DisplayStructure() {
        frame = new JFrame();
    }

    public void paint(Graphics g) {
        super.paint(g);
        renderer.paintMolecule(this.mol, (Graphics2D) g, false, false);
    }

    public void draw(IAtomContainer atomContainer) {
        if (!DrawSettings.getInstance().isWithH()) {
            mol = AtomContainerManipulator.removeHydrogens(mol);
        }

        // do aromaticity detection
        try {
            CDKHueckelAromaticityDetector.detectAromaticity(mol);
        } catch (CDKException cdke) {
            cdke.printStackTrace();
        }


        r2dm = new Renderer2DModel();
        renderer = new Renderer2D(r2dm);
        Dimension screenSize = new Dimension(DrawSettings.getInstance().getWidth(), DrawSettings.getInstance().getHeight());
        setPreferredSize(screenSize);
        r2dm.setBackgroundDimension(screenSize); // make sure it is synched with the JPanel size
        setBackground(r2dm.getBackColor());


        try {
            StructureDiagramGenerator sdg = new StructureDiagramGenerator();
            sdg.setMolecule((IMolecule) atomContainer);
            sdg.generateCoordinates();
            this.mol = sdg.getMolecule();

            r2dm.setDrawNumbers(false);
            r2dm.setUseAntiAliasing(true);
            r2dm.setColorAtomsByType(false);
            r2dm.setShowImplicitHydrogens(false);
            r2dm.setShowAromaticity(true);
            r2dm.setShowReactionBoxes(false);
            r2dm.setKekuleStructure(false);

            GeometryTools.translateAllPositive(mol);
            GeometryTools.scaleMolecule(mol,
                    getPreferredSize(),
                    DrawSettings.getInstance().getScale());
            GeometryTools.center(mol, getPreferredSize());

        } catch (Exception exc) {
            exc.printStackTrace();
        }
        frame.getContentPane().add(this);
        frame.pack();
    }

}
