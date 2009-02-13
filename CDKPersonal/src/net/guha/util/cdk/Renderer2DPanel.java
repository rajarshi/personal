package net.guha.util.cdk;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.controller.ControllerHub;
import org.openscience.cdk.controller.ControllerModel;
import org.openscience.cdk.controller.IViewEventRelay;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.RenderingParameters;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.HighlightGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.RingGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
import org.openscience.cdk.renderer.visitor.IDrawVisitor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * A JPanel to display 2D depictions.
 * <p/>
 * @author Rajarshi Guha
 */
public class Renderer2DPanel extends JPanel implements IViewEventRelay {
    private org.openscience.cdk.renderer.Renderer renderer;
    private boolean isNewChemModel;
    private ControllerHub hub;
    private boolean shouldPaintFromCache;
    RendererModel rendererModel;

    /**
     * Create an instance of the rendering panel.
     * <p/>
     * This is a simplified constructor that uses defaults for the molecule
     * title and activity. Also it does not allow one to highlight substructures.
     * The diagram will display colored atoms and will have a white background.
     *
     * @param mol molecule to render. Should have 2D coordinates
     * @param x   width of the panel
     * @param y   height of the panel
     */
    public Renderer2DPanel(IAtomContainer mol, int x, int y) {
        this(mol, null, x, y, true, Color.white);
    }

    /**
     * Create an instance of the rendering panel.
     *
     * @param mol    molecule to render. Should have 2D coordinates
     * @param needle A fragment representing a substructure of the above molecule.
     *               This substructure will be highlighted in the depiction. If no substructure
     *               is to be highlighted, then set this to null
     * @param x      width of the panel
     * @param y      height of the panel
     * @param showAtomColors should atoms be colored?
     * @param backgroundColor requested background color
     */
    public Renderer2DPanel(IAtomContainer mol, IAtomContainer needle, int x, int y,
                           boolean showAtomColors, Color backgroundColor) {
        setPreferredSize(new Dimension(x, y));
        setBackground(backgroundColor);

        IMoleculeSet moleculeSet = DefaultChemObjectBuilder.getInstance().newMoleculeSet();
        moleculeSet.addMolecule((IMolecule) mol);
        IChemModel chemModel = DefaultChemObjectBuilder.getInstance().newChemModel();
        chemModel.setMoleculeSet(moleculeSet);

        rendererModel = new RendererModel();
        rendererModel.setShowAromaticity(true);

        java.util.List<IGenerator> generators = new ArrayList<IGenerator>();
        generators.add(new RingGenerator(rendererModel));
        generators.add(new BasicAtomGenerator(rendererModel));
        generators.add(new HighlightGenerator(rendererModel));
//        generators.add(new ExternalHighlightGenerator(rendererModel));

        renderer = new org.openscience.cdk.renderer.Renderer(generators, new AWTFontManager());

        ControllerModel controllerModel = new ControllerModel();
        hub = new ControllerHub(controllerModel, renderer, chemModel, this);
        hub.getRenderer().getRenderer2DModel().setColorAtomsByType(showAtomColors);
        hub.getRenderer().getRenderer2DModel().setShowAromaticity(true);
        hub.getRenderer().getRenderer2DModel().setFitToScreen(true);
        hub.getRenderer().getRenderer2DModel().setUseAntiAliasing(true);
        hub.getRenderer().getRenderer2DModel().setBackColor(backgroundColor);
        hub.getRenderer().getRenderer2DModel().setZoomFactor(0.9);

        if (needle != null) {            
//            hub.getRenderer().getRenderer2DModel().getSelection().select(needle);
            hub.getRenderer().getRenderer2DModel().setExternalSelectedPart(needle);
//            hub.getRenderer().getRenderer2DModel().setExternalHighlightColor(Color.red);
            hub.getRenderer().getRenderer2DModel().setHighlightRadiusModel(0);
            hub.getRenderer().getRenderer2DModel().setSelectionShape(RenderingParameters.AtomShape.SQUARE);
        }

        isNewChemModel = true;

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

        IChemModel chemModel = hub.getIChemModel();
        if (chemModel != null && chemModel.getMoleculeSet() != null) {
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
        IDrawVisitor drawVisitor = new AWTDrawVisitor(g);
        renderer.paintChemModel(chemModel, drawVisitor, bounds, isNewChemModel);
        isNewChemModel = false;

        /*
         * This is dangerous, but necessary to allow fast
         * repainting when scrolling the canvas.
         *
         * I set this to false, but the original code has it set to true.
         * If set to true, then any change in dimensions requires a call to
         * updateView() - by setting to false, we don't need to call updateView()
         */
        this.shouldPaintFromCache = false;
    }

    public void setIsNewChemModel(boolean isNewChemModel) {
        this.isNewChemModel = isNewChemModel;
    }

    public void paint(Graphics g) {
        this.setBackground(renderer.getRenderer2DModel().getBackColor());
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (this.shouldPaintFromCache) {
            this.paintFromCache(g2);
        } else {
            this.paintChemModel(g2, this.getBounds());
            this.paintChemModel(g2, new Rectangle(0, 0, getWidth(), getHeight()));

        }
    }

    private void paintFromCache(Graphics2D g) {
        renderer.repaint(new AWTDrawVisitor(g));
    }

    public void updateView() {
        this.shouldPaintFromCache = false;
        this.repaint();
    }
}
