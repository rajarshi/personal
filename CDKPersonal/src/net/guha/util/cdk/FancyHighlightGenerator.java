package net.guha.util.cdk;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.elements.TextElement;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;

import javax.vecmath.Point2d;
import java.awt.*;

public class FancyHighlightGenerator extends BasicBondGenerator {

    private Color fancyColor = new Color(1.0f, 0.0f, 0.0f, 0.65f);
    private Color fancyColorSolid = new Color(1.0f, 0.0f, 0.0f, 1.0f);

    public IRenderingElement generate(IAtomContainer ac, RendererModel model) {
        ElementGroup group = new ElementGroup();
        IAtomContainer selection = model.getExternalSelectedPart();
        if (selection == null) return group;

        super.ringSet = super.getRingSet(selection);

        // crude, but works
        double originalWidth = model.getBondWidth();
        model.setBondWidth(originalWidth * 1.5);
        super.setOverrideColor(fancyColor);
        for (IBond bond : selection.bonds()) {
            group.add(super.generate(bond, model));
        }
        super.setOverrideColor(null);
        model.setBondWidth(originalWidth);

        // now do the atoms
        double originalRadius = model.getAtomRadius() / model.getScale();
        for (IAtom atom : selection.atoms()) {
            Point2d p = atom.getPoint2d();
            group.add(
                    new OvalElement(
                            p.x, p.y, originalRadius * 0, fancyColor));
        }

        for (IAtom atom : selection.atoms()) {
            Point2d p = atom.getPoint2d();
            if (!atom.getSymbol().equals("C"))
                group.add(new TextElement(p.x, p.y, atom.getSymbol(), fancyColorSolid));
        }

        return group;
    }

}

