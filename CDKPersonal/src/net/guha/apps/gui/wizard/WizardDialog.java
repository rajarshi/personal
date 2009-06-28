package net.guha.apps.gui.wizard;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import net.guha.apps.gui.wizard.stateui.StateAssaySetupUI;
import net.guha.apps.gui.wizard.stateui.StateDataLoadUI;
import net.guha.apps.gui.wizard.stateui.StateDoneUI;
import net.guha.apps.gui.wizard.stateui.StateGAFeatSelUI;
import net.guha.apps.gui.wizard.stateui.StateLoginUI;
import net.guha.apps.gui.wizard.stateui.StateReductionUI;
import net.guha.apps.gui.wizard.stateui.WizardStateUI;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Generates the complete UI for a given state of the wizard.
 * <p/>
 * The stratgey here is to simply generate the UI and set various variables based on the users action in the UI. The
 * caller of this class can then query this class to see what type of action happened and then perform the appropriate
 * tasks.
 * <p/>
 * Thus the caller will bring up a dialog based on this class and after the user has clicked next, back, cancel or done,
 * the dialog is dispose()'ed and the user can then prform the required actions. As a result this dialog will not block
 * the EDT.
 *
 * @author Rajarshi Guha
 */
public class WizardDialog extends JDialog {

    private int minX = 560;
    private int minY = 300;

    private JButton nextButton;
    private JButton backButton;

    private boolean nextClicked = false;
    private boolean backClicked = false;
    private boolean cancelClicked = false;
    private boolean stopClicked = false;

    private WizardStateUI currentStateUI;

    public JButton getNextButton() {
        return nextButton;
    }

    public JButton getBackButton() {
        return backButton;
    }

    public boolean isNextClicked() {
        return nextClicked;
    }

    public boolean isBackClicked() {
        return backClicked;
    }

    public boolean isCancelClicked() {
        return cancelClicked;
    }

    public boolean isStopClicked() {
        return stopClicked;
    }

    public WizardStateUI getCurrentStateUI() {
        return currentStateUI;
    }

    public WizardDialog(int whichState,
                        String title) {
        super();

        BorderLayout layout = new BorderLayout(5, 5);
        JPanel contentPanel = new JPanel(layout);
        Border border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        contentPanel.setBorder(border);
        setContentPane(contentPanel);
        setModal(true);
        setTitle(title);
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int width = getWidth();
                int height = getHeight();
                boolean resize = false;
                if (width < minX) {
                    width = minX;
                    resize = true;
                }
                if (height < minY) {
                    height = minY;
                    resize = true;
                }
                if (resize) setSize(width, height);
            }
        });

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                cancelClicked = true;
                dispose();
            }
        });

        //
        // Set up the buttons for the Wizard
        //
        JButton stopButton = new JButton("Stop Wizard");
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopClicked = true;
                dispose();
            }
        });

        nextButton = new JButton("Next");
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nextClicked = true;
                dispose();
            }
        });

        backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                backClicked = true;
                dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancelClicked = true;
                dispose();
            }
        });

        ButtonBarBuilder builder = new ButtonBarBuilder();
        builder.addGriddedButtons(new JButton[] {cancelButton, stopButton});
        builder.addUnrelatedGap();
        builder.addGlue();
        builder.addGriddedButtons(new JButton[] {nextButton, backButton});                                
        JPanel buttonPanel = builder.getPanel();


        //
        // Add various components to the contentPane
        //
        WizardSidebar sideBar = new WizardSidebar(WizardStates.STATE_NAMES, whichState);
        getContentPane().add(sideBar, BorderLayout.WEST);
        getContentPane().add(buttonPanel, BorderLayout.PAGE_END);

        // see which state UI we should add
        switch (whichState) {
            case WizardStates.STATE_LOGIN:
                backButton.setEnabled(false);
                currentStateUI = new StateLoginUI();
                break;
            case WizardStates.STATE_ASSAY_SETUP:
                currentStateUI = new StateAssaySetupUI();
                break;
            case WizardStates.STATE_DATA_LOAD:
                currentStateUI = new StateDataLoadUI();
                break;
            case WizardStates.STATE_REDU:
                currentStateUI = new StateReductionUI();
                break;
            case WizardStates.STATE_FSEL:
                currentStateUI = new StateGAFeatSelUI();
                break;
            case WizardStates.STATE_END:
                currentStateUI = new StateDoneUI();
                nextButton.setText("Done");
                stopButton.setEnabled(false);
                break;
        }

        // add the state UI to the content pane
        getContentPane().add(currentStateUI.getPanel(), BorderLayout.CENTER);
    }
}
