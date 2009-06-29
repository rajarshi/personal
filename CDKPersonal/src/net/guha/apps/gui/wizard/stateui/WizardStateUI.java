package net.guha.apps.gui.wizard.stateui;


import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rajarshi Guha
 */
public abstract class WizardStateUI {

    protected JPanel panel;

    /**
     * Settings that will be shared by all subclass instances.
     * <p/>
     * Allows individual states to ommunicate with each other. Instantiated when the first state UI is created.
     */
    protected static Map<String, Object> settings = null;

    /**
     * Constructor for all wizard states.
     * <p/>
     * Since this class is abstract, it cannot be instantiated. However any implementing wizard state will need to call
     * this constructor.
     */
    public WizardStateUI() {
        if (settings == null) {
            settings = new HashMap<String, Object>();
        }
    }

    /**
     * Get the JPanel containing the UI for this state.
     * <p/>
     * We cannot implement this method here by returning a field of this class since the UI designer requires that a
     * Component be bound to a field of the class it is bound to. As a result we cannot put the JPanel field of
     * subclasses in this class.
     *
     * @return The JPanel containing the UI for this state.
     */
    public JPanel getPanel() {
        return panel;
    }

    public Map<String, Object> getSettingsMap() {
        return settings;
    }

    /**
     * Evaluate the state.
     *
     * @return If evaluation suceeded then the return value is non-null and can be a Column, List of Columns or other
     *         arbitrary objects. These are stored by the caller for undo purposes. If the evaluation failed the return
     *         value is <code>null</code>
     */
    public abstract Object evaluate();

    public abstract JLabel getLabel();

    /**
     * Get and icon from an image resource.
     *
     * @param path        The path to the image
     * @param description The text that goes with the image
     * @return An {@link ImageIcon} is the image was loaded correctly, else null.
     */
    protected ImageIcon getImageIcon(String path, String description, double scale) {
        URL imgURL = WizardStateUI.class.getResource(path);
//        String imgURL = path;
        if (imgURL != null) {
            ImageIcon imageIcon = new ImageIcon(imgURL, description);

            int w = (int) (imageIcon.getIconWidth() * scale);
            int h = (int) (imageIcon.getIconHeight() * scale);
            Image image = imageIcon.getImage().getScaledInstance(w,h,Image.SCALE_AREA_AVERAGING);

            return new ImageIcon(image);
        } else return null;
    }
}
