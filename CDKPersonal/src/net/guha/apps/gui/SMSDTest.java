package net.guha.apps.gui;

import chemlib.ebi.core.tools.EBIAtomContainerManipulator;
import chemlib.ebi.core.tools.EBIMCSCalculator;
import net.guha.util.cdk.Misc;
import net.guha.util.cdk.Renderer2DPanel;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.SmilesParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;


public class SMSDTest extends JFrame {
    JTextField molfield1;
    JTextField molfield2;
    JPanel structurePane;
    JRadioButton rbCommon;
    JRadioButton rbDifference;

    boolean doCommon = true;

    public SMSDTest() {
        setTitle("SMSDTest");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout());
        setSize(300, 300);

        molfield1 = new JTextField(50);
        molfield2 = new JTextField(50);
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(molfield1, BorderLayout.NORTH);
        textPanel.add(molfield2, BorderLayout.SOUTH);

        setKeyControls(molfield1);
        setKeyControls(molfield2);

        JPanel optionPanel = new JPanel();
        rbCommon = new JRadioButton("Common SS");
        rbDifference = new JRadioButton("Difference SS");
        rbCommon.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(rbCommon);
        group.add(rbDifference);
        optionPanel.add(rbCommon);
        optionPanel.add(rbDifference);

        rbCommon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doCommon = true;
            }
        });

        rbDifference.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doCommon = false;
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(optionPanel, BorderLayout.NORTH);
        inputPanel.add(textPanel, BorderLayout.SOUTH);

        content.add(inputPanel, BorderLayout.NORTH);

        structurePane = new JPanel(new GridBagLayout(), true);
        JScrollPane scrollpane = new JScrollPane(structurePane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        content.add(scrollpane, BorderLayout.CENTER);

        JButton go = new JButton("Go");
        go.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doMCSS();
                } catch (InvalidSmilesException e1) {
                    System.out.println("e1 = " + e1);
                } catch (Exception e1) {
                    System.out.println("e1 = " + e1);
                }
            }
        });
        content.add(go, BorderLayout.SOUTH);

        setContentPane(content);
    }

    private void setKeyControls(final JTextField textField) {
        InputMap inputMap = textField.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        ActionMap actionMap = textField.getActionMap();
        actionMap.put("enter", new AbstractAction() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    doMCSS();
                } catch (Exception e) {

                }
            }
        });
        actionMap.put("escape", new AbstractAction() {
            public void actionPerformed(ActionEvent ae) {
                textField.setText("");
            }
        });
    }

    private void doMCSS() throws Exception {
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        IMolecule A1 = sp.parseSmiles(molfield1.getText().trim());
        IMolecule A2 = sp.parseSmiles(molfield2.getText().trim());

        EBIAtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(A1);
        EBIAtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(A2);

        A1 = (IMolecule) EBIAtomContainerManipulator.removeHydrogensAndPreserveAtomID(A1);
        A2 = (IMolecule) EBIAtomContainerManipulator.removeHydrogensAndPreserveAtomID(A2);

        // bond sensitive
        EBIMCSCalculator comparison = new EBIMCSCalculator(true, false);
        comparison.init(A1, A2);

        List<Integer> solution = comparison.getFirstMapping();

        // render the match
        IAtomContainer mol1 = Misc.get2DCoords(A1);
        IAtomContainer mol2 = Misc.get2DCoords(A2);

        CDKHueckelAromaticityDetector.detectAromaticity(mol1);
        CDKHueckelAromaticityDetector.detectAromaticity(mol2);

        // create the needle for the first mol. First add the
        // mapped atoms. The add the bonds

        IAtomContainer needle1 = A1.getBuilder().newAtomContainer();
        IAtomContainer needle2 = A1.getBuilder().newAtomContainer();

        if (solution == null) {
            needle1 = null;
            needle2 = null;
        } else if (solution != null && doCommon) {
            for (int i = 0; i < solution.size(); i += 2) {
                needle1.addAtom(mol1.getAtom(solution.get(i)));
            }
            for (IBond bond : mol1.bonds()) {
                if (needle1.contains(bond.getAtom(0)) &&
                        needle1.contains(bond.getAtom(1))) needle1.addBond(bond);
            }
            for (int i = 1; i < solution.size(); i += 2) {
                needle2.addAtom(mol2.getAtom(solution.get(i)));
            }
            for (IBond bond : mol2.bonds()) {
                if (needle2.contains(bond.getAtom(0)) &&
                        needle2.contains(bond.getAtom(1))) needle2.addBond(bond);
            }
        } else if (solution != null && !doCommon) {
            List<Integer> tmp = new ArrayList<Integer>();
            for (int i = 0; i < solution.size(); i += 2) tmp.add(solution.get(i));
            for (int i = 0; i < mol1.getAtomCount(); i++) {
                if (tmp.contains(i)) continue;
                else needle1.addAtom(mol1.getAtom(i));
            }
            for (IBond bond : mol1.bonds()) {
                if (needle1.contains(bond.getAtom(0)) &&
                        needle1.contains(bond.getAtom(1))) needle1.addBond(bond);
            }

            tmp.clear();
            for (int i = 1; i < solution.size(); i += 2) tmp.add(solution.get(i));
            for (int i = 0; i < mol2.getAtomCount(); i++) {
                if (tmp.contains(i)) continue;
                else needle2.addAtom(mol2.getAtom(i));
            }
            for (IBond bond : mol2.bonds()) {
                if (needle2.contains(bond.getAtom(0)) &&
                        needle2.contains(bond.getAtom(1))) needle2.addBond(bond);
            }
        }

        structurePane.removeAll();
        Renderer2DPanel panel1 = new Renderer2DPanel(mol1, needle1, 300, 300, true, Color.white);
        Renderer2DPanel panel2 = new Renderer2DPanel(mol2, needle2, 300, 300, true, Color.white);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        gbc.ipadx = 10;
        gbc.ipady = 10;

        gbc.gridx = 0;
        gbc.gridy = 0;
        structurePane.add(panel1, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        structurePane.add(panel2, gbc);


        validate();
        pack();
    }

    public static void main(String[] args) {
        SMSDTest s = new SMSDTest();
        s.setVisible(true);
    }
}
