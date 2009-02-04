package util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import util.*;

public class StructurePanel  {

    static public void showPanel(Renderer2DPanel[] data, int ncol, int cellx, int celly) {

        int pad = 5;

        int extra = data.length % ncol;
        int block = data.length - extra;
        int nrow = block / ncol;
        
        int i = 0;
        int j = 0;

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel spane = new JPanel(new GridBagLayout(), true);
        GridBagConstraints gbc = new GridBagConstraints();        
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(7,7,7,7);

        int cnt = 0;
        for (i = 0; i < nrow; i++) {
            for (j = 0; j < ncol; j++) {
                gbc.gridx = j; gbc.gridy = i; 
                spane.add( data[cnt], gbc);
                cnt += 1;
            }
        }
        j = 0;
        while (cnt < data.length) {
            gbc.gridy = nrow ; gbc.gridx = j; 
            spane.add( data[cnt], gbc);
            cnt += 1;
            j += 1;
        }

        if (extra != 0) nrow += 1;

        JScrollPane scrollpane = new JScrollPane(spane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        frame.getContentPane().add(scrollpane);

        // start the show!
        frame.pack();
        if (nrow > 3) {
            frame.setSize(ncol*cellx + pad,3*celly + pad);
        } else {
            frame.setSize(ncol*cellx + pad,nrow*celly + pad);
        }

        frame.setVisible(true);
    }
}
    
