package util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.Vector;

import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.renderer.Renderer2D;
import org.openscience.cdk.renderer.Renderer2DModel;

public class Renderer2DPanel extends JPanel
{
    AtomContainer mol;
    boolean withHydrogen = true;

    Renderer2DModel r2dm;
    Renderer2D renderer;

    public Renderer2DPanel() {
    }

    public Renderer2DPanel(AtomContainer mol, AtomContainer needle, int x, int y, boolean withHydrogen)
    {
        r2dm = new Renderer2DModel();
        renderer = new Renderer2D(r2dm);
        Dimension screenSize = new Dimension(x,y);
        setPreferredSize(screenSize);
        r2dm.setBackgroundDimension(screenSize); // make sure it is synched with the JPanel size
        setBackground(r2dm.getBackColor());

        this.mol = mol;
        this.withHydrogen = withHydrogen;

        if (!this.withHydrogen) {// get rid of hydrogens
            Vector ra = new Vector();
            for (int i = 0; i < this.mol.getAtomCount(); i++) {
                if (this.mol.getAtomAt(i).getSymbol().equals("H")) ra.add( this.mol.getAtomAt(i) );
            }
            for (int i = 0; i < ra.size(); i++) {
                this.mol.removeAtomAndConnectedElectronContainers( (Atom)ra.get(i) );
            }
        }


        try
        {
            r2dm.setDrawNumbers(false);
            r2dm.setUseAntiAliasing(true);
            r2dm.setColorAtomsByType(false);
            r2dm.setShowImplicitHydrogens(false);
            r2dm.setShowAromaticity(true);

            if (needle != null) {
                r2dm.setHighlightColor(Color.green); 
                r2dm.setSelectedPart( needle );
            }

            GeometryTools.translateAllPositive(this.mol);
            GeometryTools.scaleMolecule(this.mol, getPreferredSize(), 0.8);			
            GeometryTools.center(this.mol, getPreferredSize());
        }
        catch(Exception exc)
        {
            exc.printStackTrace();		
        }
    }
    public void paint(Graphics g)
    {
        super.paint(g);
        renderer.paintMolecule(this.mol, (Graphics2D)g);
    }
}

