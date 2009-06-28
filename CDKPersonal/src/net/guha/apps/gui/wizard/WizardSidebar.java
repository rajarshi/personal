package net.guha.apps.gui.wizard;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.net.URL;

/**
 * A sidebar that can be placed in a wizard.
 * <p/>
 * The purpose of this sidebar is to provide the user with feedback about the current state of the wizard. Currently it
 * jsut uses plain labels which are enabled or disabled depending on the current state.
 * <p/>
 * Fancier labels would be nice but I have zero artistic talent
 *
 * @author Rajarshi Guha
 */
public class WizardSidebar extends GradientPanel {
    private JLabel[] labels;
    private int currentLabel = 0;

    private ImageIcon iconOK = null;
    private ImageIcon iconRunning = null;
    private ImageIcon iconStart = null;

    /**
     * Instantiates the side bar.
     *
     * @param stateLabels An array containing the labels for each possible state, in the desired order.
     */
    public WizardSidebar(JLabel[] stateLabels) {

        super();

        iconOK = getImageIcon("images/applet-okay.png", "Success");
        iconRunning = getImageIcon("images/applet-busy.png", "Busy");
        iconStart = getImageIcon("images/start.png", "Starting");

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


        CompoundBorder border = new CompoundBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
                new EmptyBorder(10, 5, 5, 5));
        this.setBorder(border);


        currentLabel = 0;

        labels = stateLabels;
        for (JLabel label : labels) {
            label.setEnabled(false);
            this.add(label);
        }       
    }

    /**
     * Highlight the label for the current state.
     * <p/>
     * The argument is the state name for the current state that should be highlighted.
     *
     * @param s The name for the current state.
     */
    public void inState(JLabel s) {
        for (JLabel label : labels) {
            if (label.isEnabled()) label.setEnabled(false);
            if (label.getText().equals(s.getText())) label.setEnabled(true);
        }
    }

    private ImageIcon getImageIcon(String path, String description) {
        URL imgURL = WizardSidebar.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else return null;
    }

    /**
     * Make the next label in the state sequence enabled.
     */
    public void enableNextLabel() {
        labels[currentLabel].setIcon(iconOK);

        if (currentLabel == WizardStates.STATE_END) return;

        labels[currentLabel].setEnabled(false);
        currentLabel++;
        labels[currentLabel].setEnabled(true);
    }

    /**
     * Make the previous label in the state sequence enabled.
     */
    public void enablePreviousLabel() {
        labels[currentLabel].setIcon(iconStart);

        if (currentLabel == WizardStates.STATE_LOGIN) return;

        labels[currentLabel].setEnabled(false);
        currentLabel--;
        labels[currentLabel].setEnabled(true);
        labels[currentLabel].setIcon(iconStart);
    }

    public void setRunning() {
        labels[currentLabel].setIcon(iconRunning);
        this.repaint();
    }
}
