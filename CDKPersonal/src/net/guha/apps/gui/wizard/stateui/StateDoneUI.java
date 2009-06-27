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
                "You've successfully completed the wizard!\n";
        JEditorPane jed = new JEditorPane("text/html", txt);
        jed.setEditable(false);
        jed.setMargin(new Insets(1, 1, 1, 1));
        jed.setPreferredSize(new Dimension(350, 200));
        JScrollPane scrollPane = new JScrollPane(jed, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.CENTER);
    }


    public Object evaluate() {
        return new ArrayList();
    }
}
