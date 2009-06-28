package net.guha.apps.gui.wizard.stateui;


import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import net.guha.apps.gui.wizard.WizardMessageDialog;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Rajarshi Guha
 */
public class StateLoginUI extends WizardStateUI {

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
        String portString = portField.getText();
        String hostname = hostNameField.getText();
        String username = userNameField.getText();
        String password = passwordField.getText();
        String sid = sidField.getText();

        if (portString == null ||
                hostname == null ||
                username == null ||
                password == null ||
                sid == null) return null;


        System.out.println("password = " + password);
        try {
            Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            WizardMessageDialog dlg = new WizardMessageDialog("Port must be an integer");
            dlg.pack();
            dlg.setVisible(true);
            return null;
        }

        // make the connection
        try {
            conn = DriverManager.getConnection
                    ("jdbc:oracle:thin:@"+hostname+":"+portString+":"+sid, username, password);
        } catch (SQLException e) {
            WizardMessageDialog dlg = new WizardMessageDialog("<html>Couldn't connect to the database<p><pre>"
                    +e.toString()+"</pre></html>");
            dlg.pack();
            dlg.setVisible(true);
//            return null;
        }


        return new Object();
    }

    /**
     * Return the user visible name of the state.
     *
     * @return The name of the state.
     */
    public String getStateName() {
        return "Login";
    }

    public Connection getConnection() {
        return conn;
    }
}