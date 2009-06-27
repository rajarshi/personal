package net.guha.apps.gui.wizard;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.net.URL;

/**
 * A sidebar that can be placed in a wizard.
 * <p/>
 * The purpose of this sidebar is to provide the user with feedback about
 * the current state of the wizard. Currently it jsut uses plain labels which
 * are enabled or disabled depending on the current state.
 * <p/>
 * Fancier labels would be nice but I have zero artistic talent
 *
 * @author Rajarshi Guha
 */
public class WizardSidebar extends JPanel {
    private JLabel[] labels;
    private int currentLabel = 0;

    private ImageIcon iconOK = null;
    private ImageIcon iconRunning = null;
    private ImageIcon iconStart = null;

    /**
     * Instantiates the side bar.
     *
     * @param stateStrings An array containing the strings for each possible state.
     * @param startState   The state to start with
     */
    public WizardSidebar(String[] stateStrings, int startState) {

        iconOK = getImageIcon("images/applet-okay.png", "Success");
        iconRunning = getImageIcon("images/applet-busy.png", "Busy");
        iconStart = getImageIcon("images/start.png", "Starting");

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


        CompoundBorder border = new CompoundBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
                new EmptyBorder(10, 5, 5, 5));
        this.setBorder(border);


        currentLabel = startState;

        labels = new JLabel[stateStrings.length];
        for (int i = 0; i < stateStrings.length; i++) {
            labels[i] = new JLabel(stateStrings[i], iconStart, JLabel.CENTER);
            labels[i].setEnabled(false);
            this.add(labels[i]);
        }
        labels[currentLabel].setEnabled(true);
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

        if (currentLabel == WizardStates.STATE_INIT) return;

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
