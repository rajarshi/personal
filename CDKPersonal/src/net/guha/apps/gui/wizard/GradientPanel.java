package net.guha.apps.gui.wizard;

import javax.swing.*;
import java.awt.*;

/**
 * JPanel with a gradient background.
 * <p/>
 * Taken from http://nadeausoftware.com/node/82
 */
public class GradientPanel extends JPanel {

    Color color1 = null;
    Color color2 = null;

    public GradientPanel() {
        super();
    }

    public GradientPanel(LayoutManager layoutManager, boolean b) {
        super(layoutManager, b);
    }

    public GradientPanel(LayoutManager layoutManager) {
        super(layoutManager);
    }

    public GradientPanel(boolean b) {
        super(b);
    }

    public void setStartColor(Color color1) {
        this.color1 = color1;
    }

    public void setEndColor(Color color2) {
        this.color2 = color2;
    }

    protected void paintComponent(Graphics g) {
        if (!isOpaque()) {
            super.paintComponent(g);
            return;
        }

        if (color1 == null) color1 = getBackground();
        if (color2 == null) color2 = color1.darker();

        Graphics2D g2d = (Graphics2D) g;

        int w = getWidth();
        int h = getHeight();
        GradientPaint gp = new GradientPaint(
                0, 0, color1,
                0, h, color2);

        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);

        setOpaque(false);
        super.paintComponent(g);
        setOpaque(true);
    }
}
