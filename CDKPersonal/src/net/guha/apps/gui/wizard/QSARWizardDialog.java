package net.guha.apps.gui.wizard;

import net.guha.apps.gui.wizard.stateui.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;

/**
 * Generates the complete UI for a given state of the wizard.
 * <p/>
 * The stratgey here is to simply generate the UI and set various variables
 * based on the users action in the UI. The caller of this class can then query
 * this class to see what type of action happened and then perform the appropriate
 * tasks.
 * <p/>
 * Thus the caller will bring up a dialog based on this class and after the
 * user has clicked next, back, cancel or done, the dialog is dispose()'ed and
 * the user can then prform the required actions. As a result this dialog
 * will not block the EDT.
 *
 * @author Rajarshi Guha
 */
public class QSARWizardDialog extends JDialog {

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

    public QSARWizardDialog(int whichState,
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


        FlowLayout buttonPanelLayout = new FlowLayout(FlowLayout.RIGHT);
        JPanel buttonPanel = new JPanel(buttonPanelLayout);
        buttonPanel.add(nextButton);
        buttonPanel.add(backButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(stopButton);


        //
        // Add various components to the contentPane
        //
        WizardSidebar sideBar = new WizardSidebar(WizardStates.STATE_NAMES, whichState);
        getContentPane().add(sideBar, BorderLayout.WEST);
        getContentPane().add(buttonPanel, BorderLayout.PAGE_END);

        // see which state UI we should add
        switch (whichState) {
            case WizardStates.STATE_INIT:
                backButton.setEnabled(false);
                currentStateUI = new StateInitializeUI();
                break;
            case WizardStates.STATE_SETS:
                currentStateUI = new StateQSARSetsUI();
                break;
            case WizardStates.STATE_DESC:
                currentStateUI = new StateDescriptorsUI();
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
