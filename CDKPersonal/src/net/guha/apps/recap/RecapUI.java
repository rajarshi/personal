package net.guha.apps.recap;

import net.guha.util.cdk.Misc;
import net.guha.util.cdk.Renderer2DPanel;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


public class RecapUI extends JFrame {
    private String title = "RECAP UI";
    private JTextField textField;
    private JPanel structurePane;
    private StatusBar statusBar = new StatusBar();
    private SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

    public RecapUI() throws HeadlessException {
        setTitle(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);

        JButton button = new JButton("Fragment!");
        button.setName("fragmentButton");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doFragment(e);
                } catch (Exception e1) {
                    statusBar.setMessage("Error parsing SMILES");
                }
            }
        });

        textField = new JTextField(40);
        structurePane = new JPanel(new GridBagLayout(), true);
        JScrollPane scrollpane = new JScrollPane(structurePane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JPanel entryPanel = new JPanel(new BorderLayout());
        entryPanel.add(textField, BorderLayout.WEST);
        entryPanel.add(button, BorderLayout.EAST);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(entryPanel, BorderLayout.NORTH);
        getContentPane().add(scrollpane, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);
    }

    private void doFragment(ActionEvent e) throws Exception {
        String smiles = textField.getText().trim();
        IAtomContainer mol = sp.parseSmiles(smiles);
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
        Recap recap = new Recap();
        List<IAtomContainer> frags = recap.fragment(mol);
        String[] fragsmi = recap.getUniqueFragments(frags);

        List<IAtomContainer> ufrag = new ArrayList<IAtomContainer>();
        ufrag.add(mol);
        for (String s : fragsmi) {
            ufrag.add(sp.parseSmiles(s));
        }
        displayStructures(ufrag, 3);
    }

    private void displayStructures(List<IAtomContainer> frags, int ncol) throws Exception {
        int pad = 5;
        int cellx = 200;
        int celly = 200;

        structurePane.removeAll();

        Renderer2DPanel[] panels = new Renderer2DPanel[frags.size()];
        for (int i = 0; i < frags.size(); i++) {
            panels[i] = new Renderer2DPanel(Misc.get2DCoords(frags.get(i)), cellx, celly);
            if (i == 0) panels[i].setBorder(BorderFactory.createEtchedBorder(
                    EtchedBorder.LOWERED, Color.red, Color.gray));
        }

        int extra = panels.length % ncol;
        int block = panels.length - extra;
        int nrow = block / ncol;

        int i;
        int j;

        statusBar.setMessage(frags.size() + " fragments");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        //gbc.insets = new Insets(2, 2, 2, 2);

        int cnt = 0;
        for (i = 0; i < nrow; i++) {
            for (j = 0; j < ncol; j++) {
                gbc.gridx = j;
                gbc.gridy = i;
                structurePane.add(panels[cnt], gbc);
                cnt += 1;
            }
        }
        j = 0;
        while (cnt < panels.length) {
            gbc.gridy = nrow;
            gbc.gridx = j;
            structurePane.add(panels[cnt], gbc);
            cnt += 1;
            j += 1;
        }

        if (extra != 0)
            nrow += 1;

        // start the show!
        pack();
        if (nrow > 3) {
            setSize(ncol * cellx + pad, 3 * celly + pad);
        } else {
            setSize(ncol * cellx + pad, nrow * celly + pad);
        }
    }

    class StatusBar extends JLabel {

        /**
         * Creates a new instance of StatusBar
         */
        public StatusBar() {
            super();
//            super.setPreferredSize(new Dimension(100, 16));
            setMessage("Ready");
            setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        }

        public void setMessage(String message) {
            setText(message);
        }
    }
}
