package net.guha.apps.gui.wizard;

import com.jgoodies.forms.builder.ButtonBarBuilder;
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
    private JButton stopButton;

    private WizardSidebar sideBar;

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

    public void setCurrentStateUI(WizardStateUI ui) {
        WizardStateUI oldStateUI = currentStateUI;
        currentStateUI = ui;

        sideBar.inState(ui.getStateName());
        
        if (oldStateUI != null) getContentPane().remove(oldStateUI.getPanel());
        getContentPane().add(currentStateUI.getPanel(), BorderLayout.CENTER);
    }

    /**
     * Call to notify that the wizard is in the first step.
     */
    public void isFirstState() {
        backButton.setEnabled(false);
    }

    /**
     * Call to notify that the wizard is at the last step.
     */
    public void isLastState() {
        nextButton.setText("Done");
        stopButton.setEnabled(false);
    }

    public WizardDialog(WizardStateUI[] states,
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
        stopButton = new JButton("Stop Wizard");
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
        builder.addGriddedButtons(new JButton[]{cancelButton, stopButton});
        builder.addUnrelatedGap();
        builder.addGlue();
        builder.addGriddedButtons(new JButton[]{nextButton, backButton});
        JPanel buttonPanel = builder.getPanel();


        //
        // Add various components to the contentPane
        //
        String[] snames = new String[states.length];
        for (int i = 0; i < states.length; i++) snames[i] = states[i].getStateName();
        sideBar = new WizardSidebar(snames);

        getContentPane().add(sideBar, BorderLayout.WEST);
        getContentPane().add(buttonPanel, BorderLayout.PAGE_END);
    }
}
