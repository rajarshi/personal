package net.guha.apps.gui.wizard.stateui;


import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
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
        protocolNameField.setEditable(true);

        assayFormatList = new JComboBox(new Object[]{1536, 384, 96});
        assayFormatList.setSelectedIndex(1);
    }

    public StateAssaySetupUI() {
        super();

        initComponents();

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

        // we need to add a listener so that we can populate the
        // fields before the panel is shown, based on data that
        // (should) is made available from the preceding state ui
        builder.getPanel().addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent ancestorEvent) {
                if (protocolNameField.getItemCount() == 0) {
                    protocolNameField.addItem("kinome-test1");
                    protocolNameField.addItem("kinome-test1");
                    protocolNameField.addItem("drosophilia-kinome-test1");
                    protocolNameField.addItem("nci-cancer-1");
                    protocolNameField.addItem("hdg-ntp-2");                    
                }
            }

            public void ancestorRemoved(AncestorEvent ancestorEvent) {
            }

            public void ancestorMoved(AncestorEvent ancestorEvent) {
            }
        });
        panel = builder.getPanel();
    }


    public Object evaluate() {
        return new Object();
    }

    public JLabel getLabel() {
        JLabel label = new JLabel("Assay Setup");
        label.setIcon(getImageIcon("/net/guha/apps/gui/wizard/images/assaycreate.png", "", 0.125));
        return label;
    }
}