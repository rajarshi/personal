package net.guha.apps.gui.wizard.stateui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.util.ArrayList;

/**
 * @author Rajarshi Guha
 */
public class StateQSARSetsUI extends WizardStateUI {
    private static Log m_log = LogFactory.getLog(StateQSARSetsUI.class);

    public StateQSARSetsUI() {
        super();

        // panel = get a JPanel containing the ui
        panel = new JPanel();
    }

    public Object evaluate() {

        return new ArrayList();
    }

    /**
     * Return the user visible name of the state.
     *
     * @return The name of the state.
     */
    public String getStateName() {
        return "QSAR Sets";
    }

    @Override
    public JLabel getLabel() {
        return new JLabel("QSAR Sets");
    }


}
