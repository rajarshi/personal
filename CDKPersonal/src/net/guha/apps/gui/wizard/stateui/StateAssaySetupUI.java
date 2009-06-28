package net.guha.apps.gui.wizard.stateui;


import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.sql.Connection;

/**
 * @author Rajarshi Guha
 */
public class StateAssaySetupUI extends WizardStateUI {

    private JTextField assayNameField;
    private JTextField runSetField;
    private JComboBox protocolNameField;
    private JComboBox assayFormatList;

    private Connection conn;

    private void initComponents() {
        assayNameField = new JTextField();        
        runSetField = new JTextField();
        protocolNameField = new JComboBox();
        assayFormatList = new JComboBox(new Object[]{new Integer(1536), new Integer(384), new Integer(96)});
        assayFormatList.setSelectedIndex(1);

    }

    public StateAssaySetupUI() {
        super();

        initComponents();

//        FormLayout layout = new FormLayout("right:[40dlu,pref], 3dlu, 70dlu, 7dlu, right:[40dlu,pref], 3dlu, 70dlu");
        FormLayout layout = new FormLayout("right:[40dlu,pref], 3dlu, pref:grow");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Assay Setup");

        builder.append("Assay Name", assayNameField);
        builder.nextLine();

        builder.append("Protocol Name", protocolNameField);
        builder.nextLine();

        builder.append("Run Set", runSetField);
        builder.nextLine();

        builder.append("Assay Format", assayFormatList);
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
        return "Assay Setup";
    }
}