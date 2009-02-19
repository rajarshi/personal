package net.guha.util.cdk;

import javax.swing.*;
import javax.swing.border.BevelBorder;

public class StatusBar extends JLabel {
    public StatusBar() {
        super();
        setMessage("Ready");
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    }

    public void setMessage(String message) {
        setText(message);
    }
}
