package net.guha.apps.gui.wizard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class WizardMessageDialog extends JDialog {

    public WizardMessageDialog(String message) {
        setTitle("Wizard Message");
        
        JPanel contentPane = new JPanel(new BorderLayout());

        JButton buttonOK = new JButton("OK");
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        JLabel messageLabel = new JLabel();

        contentPane.add(messageLabel, BorderLayout.CENTER);
        contentPane.add(buttonOK, BorderLayout.SOUTH);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        messageLabel.setText(message);


    }

    private void onOK() {
        dispose();
    }
}
