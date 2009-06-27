package net.guha.apps.gui.wizard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;

/**
 * @author Rajarshi Guha
 */
public class QSARWizardGUILoop {
    private static Log m_log = LogFactory.getLog(QSARWizardGUILoop.class);


    public QSARWizardGUILoop() {
        m_log.info("In the EDT: " + SwingUtilities.isEventDispatchThread());

        // we clear the wizard report chunk list in case we had used before this
        WizardReportPage.getInstance().clearPage();
    }


    public void run() {
        int currentState = WizardStates.STATE_INIT;
        QSARWizardUndo undoStack = new QSARWizardUndo();

        QSARWizardDialog wdlg;
        Object ret;
        String dlgTitle = "QSAR Modeling Wizard";
        boolean wizardComplete = false;


        while (!wizardComplete) {
            wdlg = new QSARWizardDialog(currentState, dlgTitle);
            wdlg.pack();
            wdlg.setVisible(true);

            // see what action needs to be taken
            if (wdlg.isStopClicked()) {
                m_log.info(WizardStates.STATE_NAMES[currentState] + " clicked stop ");
                wizardComplete = true;
            } else if (wdlg.isNextClicked()) {
                m_log.info(WizardStates.STATE_NAMES[currentState] + " clicked next ");
                if (currentState == WizardStates.STATE_END) {
                    wizardComplete = true;
                } else {
                    ret = wdlg.getCurrentStateUI().evaluate();
                    if (ret == null) continue;
                    else undoStack.push(ret);
                    currentState++;
                }
            } else if (wdlg.isBackClicked()) {
                m_log.info(WizardStates.STATE_NAMES[currentState] + " clicked back ");
                if (currentState == WizardStates.STATE_INIT) continue;
                undoStack.undo(currentState);
                currentState--;
            } else if (wdlg.isCancelClicked()) {
                m_log.info(WizardStates.STATE_NAMES[currentState] + " clicked cancel ");
                undoStack.undoAll(currentState);
                wizardComplete = true;
            }
        } // end while

//        activeWindow.setEnabled(true);
    } // end run

    public static void main(String[] args) {
        QSARWizardGUILoop gloop = new QSARWizardGUILoop();
        gloop.run();
    }
}

