package net.guha.apps.gui.wizard;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import net.guha.apps.gui.wizard.stateui.StateAssaySetupUI;
import net.guha.apps.gui.wizard.stateui.StateDataLoadUI;
import net.guha.apps.gui.wizard.stateui.StateDoneUI;
import net.guha.apps.gui.wizard.stateui.StateLoginUI;
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


    public WizardGUILoop() {
        m_log.info("In the EDT: " + SwingUtilities.isEventDispatchThread());
        WizardReportPage.getInstance().clearPage();
    }

    public WizardGUILoop(WizardStateUI[] states) {
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

    public void run() {
        int currentState = 0; // beginning of the state list
        WizardUndo undoStack = new WizardUndo();

        WizardDialog wdlg;
        Object ret;
        String dlgTitle = "Wizard";
        boolean wizardComplete = false;


        while (!wizardComplete) {
            wdlg = new WizardDialog(currentState, dlgTitle);
            wdlg.pack();
            wdlg.setVisible(true);

            // see what action needs to be taken
            if (wdlg.isStopClicked()) {
                m_log.info(states[currentState].getStateName() + " clicked stop ");
                wizardComplete = true;
            } else if (wdlg.isNextClicked()) {
                m_log.info(states[currentState].getStateName() + " clicked next ");
                if (currentState == states.length-1) {
                    wizardComplete = true;
                } else {
                    ret = wdlg.getCurrentStateUI().evaluate();
                    if (ret == null) continue;
                    else undoStack.push(ret);
                    currentState++;
                }
            } else if (wdlg.isBackClicked()) {
                m_log.info(states[currentState].getStateName() + " clicked back ");
                if (currentState == 0) continue;
                undoStack.undo(currentState);
                currentState--;
            } else if (wdlg.isCancelClicked()) {
                m_log.info(states[currentState].getStateName() + " clicked cancel ");
                undoStack.undoAll(currentState);
                wizardComplete = true;
            }
        } // end while

    } // end run

    public static void main(String[] args) {
        WizardStateUI[] states = new WizardStateUI[]{
                new StateLoginUI(),
                new StateAssaySetupUI(),
                new StateDataLoadUI(),
                new StateDoneUI()
        };
        
        WizardGUILoop gloop = new WizardGUILoop(states);
        gloop.configureUI();
        gloop.run();
    }
}

