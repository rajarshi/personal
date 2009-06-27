package net.guha.apps.gui.wizard.stateui;


import javax.swing.*;

/**
 * @author Rajarshi Guha
 */
public abstract class WizardStateUI {

    protected JPanel panel;

    /**
     * Constructor for all wizard states.
     * <p/>
     * Since this class is abstract, it cannot be instantiated. However any implementing
     * wizard state will need to call this constructor.
     *
     */
    public WizardStateUI() {
    }

    /**
     * Get the JPanel containing the UI for this state.
     * <p/>
     * We cannot implement this method here by returning a field of this class
     * since the UI designer requires that a Component be bound to a field of
     * the class it is bound to. As a result we cannot put the JPanel field of
     * subclasses in this class.
     *
     * @return The JPanel containing the UI for this state.
     */
    public JPanel getPanel() {
        return panel;
    }

    /**
     * Evaluate the state.
     *
     * @return If evaluation suceeded then the return value is non-null and can be a Column,
     *         List of Columns or other arbitrary objects. These are stored by the caller for undo
     *         purposes. If the evaluation failed the return value is <code>null</code>
     */
    public abstract Object evaluate();
}
