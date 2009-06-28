package net.guha.apps.gui.wizard.stateui;


import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author Rajarshi Guha
 */
public class StateInitializeUI extends WizardStateUI {

    public StateInitializeUI() {
        super();

        panel = new JPanel(new BorderLayout());

        CompoundBorder border = new CompoundBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
                new EmptyBorder(1, 1, 1, 1));
        panel.setBorder(border);


        String txt = "<h3>Welcome</h3>Start page of the UI";
        JEditorPane jed = new JEditorPane("text/html", txt);
        jed.setEditable(false);
        jed.setMargin(new Insets(1, 1, 1, 1));
        jed.setPreferredSize(new Dimension(350, 200));

        JScrollPane scrollPane = new JScrollPane(jed, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);

//        Action geomOptAction = new AbstractAction("Perform geometry optimization") {
//            public void actionPerformed(ActionEvent evt) {
//                JCheckBox cb = (JCheckBox) evt.getSource();
//                boolean isSel = cb.isSelected();
//                if (isSel) {
//                    doGeomOpt = true;
//                } else {
//                    doGeomOpt = false;
//                }
//            }
//        };
//        JCheckBox geomCheckBox = new JCheckBox(geomOptAction);
//        geomCheckBox.setEnabled(false);
//
//        JPanel subpanel = new JPanel(new GridBagLayout());
//        GridBagConstraints c = new GridBagConstraints();
//        c.gridx = 0;
//        c.gridy = 0;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        c.weightx = 1.0;
//        c.anchor = GridBagConstraints.WEST;
//        subpanel.add(geomCheckBox, c);
//
//        panel.add(subpanel, BorderLayout.SOUTH);
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
        return "Initialize";
    }

    @Override
    public JLabel getLabel() {
        return new JLabel("Initialize");
    }
}
