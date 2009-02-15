package net.guha.test;

import net.guha.util.cdk.Renderer2DPanel;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.layout.TemplateHandler;
import org.openscience.cdk.smiles.SmilesParser;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @cdk.author Rajarshi Guha
 * @cdk.svnrev $Revision: 9162 $
 */
public class DepictTest {

    static class ApplicationCloser extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
        }
    }

    SmilesParser smilesParser;
    StructureDiagramGenerator sdg;

    public DepictTest() {
        smilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        sdg = new StructureDiagramGenerator();
        sdg.setTemplateHandler(new TemplateHandler(DefaultChemObjectBuilder.getInstance()));
    }


    public void runCase1() throws Exception {
        String smiles = "c1cc(CC=CC#N)ccn1";
        IAtomContainer molecule = smilesParser.parseSmiles(smiles);
        sdg.setMolecule((IMolecule) molecule);
        sdg.generateCoordinates();
        molecule = sdg.getMolecule();

        Renderer2DPanel rendererPanel = new Renderer2DPanel(molecule, 200, 200);

        rendererPanel.setName("rendererPanel");
        JFrame frame = new JFrame("2D Structure Viewer");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(rendererPanel);
        frame.addWindowListener(new ApplicationCloser());
        frame.setSize(200, 200);


        frame.setVisible(true);

    }

    public static void main(String[] args) throws Exception {
        DepictTest dt = new DepictTest();
        dt.runCase1();
    }
}
