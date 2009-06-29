package net.guha.apps.gui.wizard;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import net.guha.apps.gui.wizard.stateui.StateAssaySetupUI;
import net.guha.apps.gui.wizard.stateui.StateDataLoadUI;
import net.guha.apps.gui.wizard.stateui.StateDoneUI;
import net.guha.apps.gui.wizard.stateui.StateLoginUI;
import net.guha.apps.gui.wizard.stateui.StatePlateMapUI;
import net.guha.apps.gui.wizard.stateui.WizardStateUI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.*;

/**
 * @author Rajarshi Guha
 */
public class WizardGUILoop {
    private static Log m_log = LogFactory.getLog(WizardGUILoop.class);

    private WizardStateUI[] states = null;
    private WizardDialog wdlg = null;

    public WizardGUILoop(WizardStateUI[] states) {
        m_log.info("In the EDT: " + SwingUtilities.isEventDispatchThread());
        WizardReportPage.getInstance().clearPage();
        this.states = states;
    }

    private void configureUI() {
        UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
        Options.setDefaultIconSize(new Dimension(18, 18));

        String lafName =
                LookUtils.IS_OS_MAC
                        ? Options.getCrossPlatformLookAndFeelClassName()
                        : Options.getSystemLookAndFeelClassName();

        try {
//            UIManager.setLookAndFeel(lafName);
            UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
        } catch (Exception e) {
            System.err.println("Can't set look & feel:" + e);
        }
    }

    public void initUI() {
        wdlg = new WizardDialog(states, "Wizard");
        wdlg.setCurrentStateUI(states[0]);
        wdlg.whichState(WizardDialog.STATE_FIRST);
        wdlg.pack();
        wdlg.setVisible(true);
    }

    public void run() {
        int currentState = 0; // beginning of the state list
        WizardUndo undoStack = new WizardUndo();

        Object ret;

        boolean wizardComplete = false;

        while (!wizardComplete) {

            // see what action needs to be taken
            if (wdlg.isStopClicked()) {
                wizardComplete = true;
            } else if (wdlg.isNextClicked()) {
                currentState++;

                if (currentState == states.length) {
                    wizardComplete = true;
                } else {
                    if (currentState == states.length - 1) wdlg.whichState(WizardDialog.STATE_LAST);
                    else wdlg.whichState(WizardDialog.STATE_INTERMEDIATE);
                    ret = wdlg.getCurrentStateUI().evaluate();
                    if (ret == null) continue;
                    else undoStack.push(ret);

                    // set up the next state dialog
                    wdlg.setCurrentStateUI(states[currentState]);
                }
            } else if (wdlg.isBackClicked()) {
                currentState--;

                if (currentState == 0) {
                    wdlg.whichState(WizardDialog.STATE_FIRST);
//                    continue;
                } else wdlg.whichState(WizardDialog.STATE_INTERMEDIATE);
                undoStack.undo(currentState + 1);

                // set up the next state dialog
                wdlg.setCurrentStateUI(states[currentState]);
            } else if (wdlg.isCancelClicked()) {
                undoStack.undoAll(currentState);
                wizardComplete = true;
            }
        } // end while

    } // end run

    public static void main(String[] args) {
        WizardStateUI[] states = new WizardStateUI[]{
                new StateLoginUI(),
                new StateAssaySetupUI(),
                new StatePlateMapUI(),
                new StateDataLoadUI(),
                new StateDoneUI()
        };

        WizardGUILoop gloop = new WizardGUILoop(states);
        gloop.configureUI();
        gloop.initUI();
        gloop.run();
    }
}

