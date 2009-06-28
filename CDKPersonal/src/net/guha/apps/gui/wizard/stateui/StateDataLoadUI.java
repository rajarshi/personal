package net.guha.apps.gui.wizard.stateui;


import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.sql.Connection;

/**
 * @author Rajarshi Guha
 */
public class StateDataLoadUI extends WizardStateUI {

    private JTextField hostNameField;
    private JTextField portField;
    private JTextField userNameField;
    private JTextField passwordField;
    private JTextField sidField;
    

    private Connection conn;

    private void initComponents() {

        hostNameField = new JTextField();
        portField = new JTextField();
        userNameField = new JTextField();
        passwordField = new JPasswordField();
        sidField = new JTextField();

        // we set some default values for now
        // should read these from a config file
        hostNameField.setText("ncgcprobe.nhgri.nih.gov");
        userNameField.setText("rnai");
        passwordField.setText("ncgc");
        sidField.setText("probedb");
        portField.setText("1521");

    }

    public StateDataLoadUI() {
        super();

        initComponents();

        FormLayout layout = new FormLayout(
                "right:[40dlu,pref], 3dlu, 70dlu, 7dlu, "
                        + "right:[40dlu,pref], 3dlu, 70dlu");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Data Load");

        builder.append("Hostname", hostNameField);
        builder.nextLine();

        builder.append("Port", portField);
        builder.nextLine();

        builder.append("Username", userNameField);
        builder.nextLine();

        builder.append("Password", passwordField);
        builder.nextLine();

        builder.append("SID", sidField);
        builder.nextLine();

        panel = builder.getPanel();
    }


    public Object evaluate() {
        return new Object();
    }

    /**
     * Return the user visible name of the state.
     *
     * @return The name of the state.
     */
    public String getStateName() {
        return "Data Load";
    }
}