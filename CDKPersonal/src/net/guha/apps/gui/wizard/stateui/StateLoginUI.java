package net.guha.apps.gui.wizard.stateui;


import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.util.ArrayList;

/**
 * @author Rajarshi Guha
 */
public class StateLoginUI extends WizardStateUI {

    private JTextField hostNameField;
    private JTextField portField;
    private JTextField userNameField;
    private JTextField passwordField;
    private JTextField sidField;

    private void initComponents() {
        hostNameField = new JTextField();
        portField = new JTextField();
        userNameField = new JTextField();
        passwordField = new JTextField();
        sidField = new JTextField();
    }

    public StateLoginUI() {
        super();

        initComponents();

        FormLayout layout = new FormLayout(
                "right:[40dlu,pref], 3dlu, 70dlu, 7dlu, "
                        + "right:[40dlu,pref], 3dlu, 70dlu");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Database login");

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
        return new ArrayList();
    }
}