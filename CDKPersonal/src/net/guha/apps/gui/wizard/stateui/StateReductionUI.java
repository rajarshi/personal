package net.guha.apps.gui.wizard.stateui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.util.ArrayList;

/**
 * @author Rajarshi Guha
 */
public class StateReductionUI extends WizardStateUI {
    private static Log m_log = LogFactory.getLog(StateDescriptorsUI.class);


    public StateReductionUI() {
        super();

        // panel = get ui panel
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
        return "Data Reduction";
    }

}
