package net.guha.apps.rest;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.JPEGEncodeParam;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.controller.ControllerHub;
import org.openscience.cdk.controller.ControllerModel;
import org.openscience.cdk.controller.IViewEventRelay;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.layout.TemplateHandler;
import org.openscience.cdk.renderer.IntermediateRenderer;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.swing.*;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author rguha
 */
public class StructureDiagram extends JPanel implements IViewEventRelay {
    private IntermediateRenderer renderer;

    // we set this to true since we're not doing any updating
    private boolean isNewChemModel = true;

    private ControllerHub hub;
    private ControllerModel controllerModel;
    private boolean shouldPaintFromCache;

    IMolecule mol2d = null;
    JFrame frame = null;

    public StructureDiagram() {
    }

    public void close() throws Exception {
    }

    public Image takeSnapshot() {
        return this.takeSnapshot(this.getBounds());
    }

    public Image takeSnapshot(Rectangle bounds) {
        Image image = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getScreenDevices()[0]
                .getDefaultConfiguration()
                .createCompatibleImage(bounds.width, bounds.height);
        Graphics2D g = (Graphics2D) image.getGraphics();
        super.paint(g);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        this.paintChemModel(g, bounds);
        return image;
    }

    public void paintChemModel(Graphics2D g, Rectangle screenBounds) {

        IChemModel chemModel = this.hub.getIChemModel();
        if (chemModel != null && chemModel.getMoleculeSet() != null) {

            // determine the size the canvas needs to be in order to fit the model
            Rectangle diagramBounds = renderer.calculateScreenBounds(chemModel);

            if (this.overlaps(screenBounds, diagramBounds)) {
                Rectangle union = screenBounds.union(diagramBounds);
                this.setPreferredSize(union.getSize());
                this.revalidate();
            }
            this.paintChemModel(chemModel, g, screenBounds);
        }
    }

    private boolean overlaps(Rectangle screenBounds, Rectangle diagramBounds) {
        return screenBounds.getMinX() > diagramBounds.getMinX()
                || screenBounds.getMinY() > diagramBounds.getMinY()
                || screenBounds.getMaxX() < diagramBounds.getMaxX()
                || screenBounds.getMaxY() < diagramBounds.getMaxY();
    }

    private void paintChemModel(IChemModel chemModel, Graphics2D g, Rectangle bounds) {

        // paint the chem model, and record that it is no longer new
        renderer.paintChemModel(chemModel, g, bounds, isNewChemModel);
        isNewChemModel = false;

        /*
         * This is dangerous, but necessary to allow fast
         * repainting when scrolling the canvas
         */
        this.shouldPaintFromCache = true;
    }

    public void setIsNewChemModel(boolean isNewChemModel) {
        this.isNewChemModel = isNewChemModel;
    }

    public void paint(Graphics g) {
        this.setBackground(renderer.getRenderer2DModel().getBackColor());
        super.paint(g);
        // set the graphics to antialias
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (this.shouldPaintFromCache) {
            this.paintFromCache(g2);
        } else {
            this.paintChemModel(g2, this.getBounds());
        }
    }

    private void paintFromCache(Graphics2D g) {
        renderer.repaint(g);
    }

    /**
     * Gets the 2D structure diagram of a SMILES structure.
     *
     * @param molecule The input molecule
     * @param width  The width of the image
     * @param height The height of the image
     * @param scale  The scale of the image
     * @return The bytes representing the JPEG image
     * @throws CDKException if there is an error in parsing the SMILES or generating the image
     */
    public byte[] getDiagram(IAtomContainer molecule, int width, int height, double scale) throws CDKException {
        if (width <= 0) {
            width = 300;
        }
        if (height <= 0) {
            height = 300;
        }
        if (scale <= 0) {
            scale = 0.9;
        }

        setPreferredSize(new Dimension(width, height));
        setBackground(Color.WHITE);

        ByteArrayOutputStream baos = null;
        try {
            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
            CDKHueckelAromaticityDetector.detectAromaticity(molecule);
            AtomContainerManipulator.removeHydrogens(molecule);            

            StructureDiagramGenerator sdg = new StructureDiagramGenerator();
            sdg.setTemplateHandler(new TemplateHandler(DefaultChemObjectBuilder.getInstance()));
            sdg.setMolecule((IMolecule) molecule);
            sdg.generateCoordinates(new Vector2d(0, 1));
            mol2d = sdg.getMolecule();

            IMoleculeSet moleculeSet = DefaultChemObjectBuilder.getInstance().newMoleculeSet();
            moleculeSet.addMolecule(mol2d);
            IChemModel chemModel = DefaultChemObjectBuilder.getInstance().newChemModel();
            chemModel.setMoleculeSet(moleculeSet);

            frame = new JFrame();
            renderer = new IntermediateRenderer();
            renderer.setFitToScreen(true);
            controllerModel = new ControllerModel();
            hub = new ControllerHub(controllerModel, renderer, chemModel, this);

            hub.getIJava2DRenderer().getRenderer2DModel().setShowAromaticity(true);
            hub.getIJava2DRenderer().getRenderer2DModel().setUseAntiAliasing(true);
            hub.getIJava2DRenderer().getRenderer2DModel().setIsCompact(false);
            hub.getIJava2DRenderer().getRenderer2DModel().setZoomFactor(scale);
            //hub.getIJava2DRenderer().getRenderer2DModel().setShowExplicitHydrogens(false);
            hub.getIJava2DRenderer().getRenderer2DModel().setShowImplicitHydrogens(false);

            frame.getContentPane().add(this);
            frame.pack();

            Image img = takeSnapshot();
            RenderedOp image = JAI.create("AWTImage", img);

            JPEGEncodeParam params = new JPEGEncodeParam();

            baos = new ByteArrayOutputStream();
            ImageEncoder encoder = ImageCodec.createImageEncoder("JPEG", baos, params);
            encoder.encode(image);
            baos.close();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        } catch (CDKException exc) {

            throw new CDKException(exc.toString());
        } catch (IOException e) {

            throw new CDKException("IO error" + e.toString());
        } catch (Exception e) {

        }

        // get the byte array
        return baos.toByteArray();
    }


    public void updateView() {
        this.shouldPaintFromCache = false;
        this.repaint();
    }
}
