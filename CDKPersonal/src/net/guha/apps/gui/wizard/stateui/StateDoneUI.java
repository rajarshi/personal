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
public class StateDoneUI extends WizardStateUI {


    public StateDoneUI() {
        super();

        panel = new JPanel(new BorderLayout());

        CompoundBorder border = new CompoundBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
                new EmptyBorder(2, 2, 2, 2));
        panel.setBorder(border);

        String txt = "<html>\n" +
                "<body>\n" +
                "You've successfully completed the wizard!\n" +
                "<p>\n" +
                "    All the models you've generated have been placed in seperate tabs\n" +
                "    labeled <i>Model 1</i>, <i>Model 2</i> and so on. In addition if you had\n" +
                "    instructed that plots be generated they have also been inserted as tabs.\n" +
                "</p>\n" +
                "\n" +
                "<p>\n" +
                "    Click <i>Done</i> to return to the worksheet and a summary report of the whole\n" +
                "    procedure will be added.\n" +
                "</p>\n" +
                "</body>\n" +
                "</html>";
        JEditorPane jed = new JEditorPane("text/html", txt);
        jed.setEditable(false);
        jed.setMargin(new Insets(1, 1, 1, 1));
        jed.setPreferredSize(new Dimension(350, 200));
        JScrollPane scrollPane = new JScrollPane(jed, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.CENTER);

//        JPanel boxPanel = new JPanel(new BorderLayout());
//        saveModelsBox = new JCheckBox("Save models");
//        saveDescriptorsBox = new JCheckBox("Save reduced descriptor pool");
//        saveModelsBox.setSelected(false);
//        saveDescriptorsBox.setSelected(false);
//        boxPanel.add(saveDescriptorsBox, BorderLayout.PAGE_START);
//        boxPanel.add(saveModelsBox, BorderLayout.PAGE_END);
//        panel.add(boxPanel, BorderLayout.PAGE_END);
//
//        saveModelsBox.setEnabled(false);
//        saveDescriptorsBox.setEnabled(false);

    }


    public Object evaluate() {
//        if (saveDescriptorsBox.isSelected()) {
//
//        }
//        if (saveModelsBox.isSelected()) {
//
//        }
        return new ArrayList();
    }
}
