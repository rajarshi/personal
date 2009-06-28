package net.guha.apps.gui.wizard;

/**
 * @author Rajarshi Guha
 */
public class WizardStates {

    /**
     * Names for the individual states.
     *
     * These can be indexed by the integer values of the states
     */
    public static final String[] STATE_NAMES = {
            "Login", "QSAR Sets", "Descriptor Evaluation",
            "Descriptor Reduction", "Feature Selection", "Done"
    };

    /**
     * The maimum number of possible states.
     */
    public static final int MAX_STATES = STATE_NAMES.length;

    /**
     * The start (initialization) state.
     * <p/>
     * Just displays a message.
     */
    public static final int STATE_LOGIN = 0;

    /**
     * The set selection state.
     */
    public static final int STATE_SETS = 1;

    /**
     * The descriptor evaluation state.
     */
    public static final int STATE_DESC = 2;

    /**
     * The descriptor reduction state.
     * <p/>
     * This will insert a new worksheet into the current document.
     */
    public static final int STATE_REDU = 3;

    /**
     * The feature selection state.
     */
    public static final int STATE_FSEL = 4;

    /**
     * The end state.
     * <p/>
     * Simply displays a message and nothing else.
     */
    public static final int STATE_END = 5;

}
