package util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.renderer.Renderer2D;
import org.openscience.cdk.renderer.Renderer2DModel;

import org.openscience.cdk.AtomContainer;


public class displaySubstructure extends JPanel
{
    AtomContainer haystack;

    Renderer2DModel r2dm;
    Renderer2D renderer;

    public displaySubstructure(AtomContainer haystack, AtomContainer needle, int width, int height)
    {
        r2dm = new Renderer2DModel();
        renderer = new Renderer2D(r2dm);
        Dimension screenSize = new Dimension(width, height);
        setPreferredSize(screenSize);
        r2dm.setBackgroundDimension(screenSize); 
        setBackground(r2dm.getBackColor());

        this.haystack = haystack;

        try
        {
            r2dm.setDrawNumbers(false);
            r2dm.setUseAntiAliasing(true);
            r2dm.setShowAromaticity(true);
            r2dm.setColorAtomsByType(false);
            r2dm.setHighlightColor(Color.green); 

            r2dm.setSelectedPart( needle );

            GeometryTools.translateAllPositive(this.haystack);
            GeometryTools.scaleMolecule(this.haystack, getPreferredSize(), 0.8);			
            GeometryTools.center(this.haystack, getPreferredSize());
        }
        catch(Exception exc)
        {
            exc.printStackTrace();		
        }
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(this);
        frame.pack();
        frame.setVisible(true);

    }

    public void paint(Graphics g)
    {
        super.paint(g);
        renderer.paintMolecule(this.haystack, (Graphics2D)g);
    }

}

