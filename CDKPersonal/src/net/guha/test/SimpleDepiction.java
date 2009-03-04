package net.guha.test;

import net.guha.util.cdk.Misc;
import net.guha.util.cdk.Renderer2DPanel;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;

import javax.swing.*;
import java.awt.*;


public class SimpleDepiction {

    public static void main(String[] args) throws Exception {
        SmilesParser smilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        String smiles = "c1cc(CC=CC#N)ccn1";
        IAtomContainer molecule = smilesParser.parseSmiles(smiles);
        molecule = Misc.get2DCoords(molecule);

        String pattern = "CC=CC~N";
        IAtomContainer needle = Misc.getNeedle(molecule, pattern);

        Renderer2DPanel rendererPanel = new Renderer2DPanel(molecule, needle, 200, 200, true, Color.white);
        rendererPanel.setName("rendererPanel");
        JFrame frame = new JFrame("2D Structure Viewer");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(rendererPanel);
        frame.setSize(200, 200);
        frame.setVisible(true);
    }
}
