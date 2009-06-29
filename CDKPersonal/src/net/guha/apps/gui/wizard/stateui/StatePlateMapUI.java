package net.guha.apps.gui.wizard.stateui;


import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.sql.Connection;

/**
 * @author Rajarshi Guha
 */
public class StatePlateMapUI extends WizardStateUI {

    private Connection conn;

    private void initComponents() {

    }

    public StatePlateMapUI() {
        super();

        initComponents();

        FormLayout layout = new FormLayout("right:[40dlu,pref], 3dlu, pref:grow");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Platemap specification");


        panel = builder.getPanel();
    }


    public Object evaluate() {

        // store the connection object so that others can get it
        settings.put("gov.nih.ncgc.rnai.setttings.connection", conn);

        return new Object();
    }

    @Override
    public JLabel getLabel() {
        JLabel label = new JLabel("Plate map");
        label.setIcon(getImageIcon("/net/guha/apps/gui/wizard/images/platemap.png", "", 0.125));
        return label;
    }
}