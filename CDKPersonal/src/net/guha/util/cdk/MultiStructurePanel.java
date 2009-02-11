package net.guha.util.cdk;

import javax.swing.*;
import java.awt.*;


public class MultiStructurePanel extends JFrame {
    public MultiStructurePanel(Renderer2DPanel[] panels,
                               int ncol, int cellx, int celly) throws HeadlessException {
        int pad = 5;

        int extra = panels.length % ncol;
        int block = panels.length - extra;
        int nrow = block / ncol;

        int i;
        int j;

        setTitle("Molecule Fragmenter");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel spane = new JPanel(new GridBagLayout(), true);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        //gbc.insets = new Insets(2, 2, 2, 2);

        int cnt = 0;
        for (i = 0; i < nrow; i++) {
            for (j = 0; j < ncol; j++) {
                gbc.gridx = j;
                gbc.gridy = i;
                spane.add(panels[cnt], gbc);
                cnt += 1;
            }
        }
        j = 0;
        while (cnt < panels.length) {
            gbc.gridy = nrow;
            gbc.gridx = j;
            spane.add(panels[cnt], gbc);
            cnt += 1;
            j += 1;
        }

        if (extra != 0)
            nrow += 1;

        JScrollPane scrollpane = new JScrollPane(spane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        getContentPane().add(scrollpane);

        // start the show!
        pack();
        if (nrow > 3) {
            setSize(ncol * cellx + pad, 3 * celly + pad);
        } else {
            setSize(ncol * cellx + pad, nrow * celly + pad);
        }
    }
}
