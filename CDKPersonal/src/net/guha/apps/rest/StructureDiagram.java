package net.guha.apps.rest;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.PNGEncodeParam;
import net.guha.util.cdk.Base64;
import net.guha.util.cdk.Misc;
import net.guha.util.cdk.Renderer2DPanel;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.smiles.smarts.parser.TokenMgrError;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author rguha
 */
public class StructureDiagram {
    IMolecule mol2d = null;
    JFrame frame = null;

    public StructureDiagram() {
    }

    public void close() throws Exception {
    }

    private IAtomContainer getNeedle(IAtomContainer target, String query) throws CDKException {
        SMARTSQueryTool sqt;

        try {
            sqt = new SMARTSQueryTool(query);
        } catch (TokenMgrError e) {
            try {
                sqt = new SMARTSQueryTool(new String(Base64.decode(query)));
            } catch (CDKException e1) {
                throw new CDKException("Couldn't parse query");
            }
        }
        if (!sqt.matches(target)) return null;
        List<List<Integer>> matches = sqt.getUniqueMatchingAtoms();
        IAtomContainer ret = target.getBuilder().newAtomContainer();
        for (List<Integer> match : matches) {
            for (Integer idx : match) ret.addAtom(target.getAtom(idx));
        }

        // need to add the bonds, we only consider 2 atom bonds
        for (IBond bond : target.bonds()) {
            IAtom atom1 = bond.getAtom(0);
            IAtom atom2 = bond.getAtom(1);
            if (ret.contains(atom1) && ret.contains(atom2)) ret.addBond(bond);
        }

        return ret;
    }

    /**
     * Gets the 2D structure diagram of a SMILES structure.
     *
     * @param molecule The input molecule
     * @param ssquery  A SMARTS pattern to be used for highlighting a substructure
     * @param width    The width of the image
     * @param height   The height of the image
     * @param scale    The scale of the image
     * @return The bytes representing the JPEG image
     * @throws CDKException if there is an error in parsing the SMILES or generating the image
     */
    public byte[] getDiagram(IAtomContainer molecule, String ssquery, int width, int height, double scale) throws CDKException {
        if (width <= 0) {
            width = 300;
        }
        if (height <= 0) {
            height = 300;
        }
        if (scale <= 0) {
            scale = 0.9;
        }

        ByteArrayOutputStream baos = null;
        try {
            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
            CDKHueckelAromaticityDetector.detectAromaticity(molecule);
            AtomContainerManipulator.removeHydrogens(molecule);
            mol2d = Misc.get2DCoords(molecule);

            IAtomContainer needle = null;
            if (ssquery != null) needle = getNeedle(mol2d, ssquery);

            Renderer2DPanel panel = new Renderer2DPanel(mol2d, needle, width, height, true, Color.white);
            frame = new JFrame();
            frame.getContentPane().add(panel);
            frame.pack();
            Image img = panel.takeSnapshot();
            RenderedOp image = JAI.create("AWTImage", img);
            PNGEncodeParam params = new PNGEncodeParam.RGB();
            ((PNGEncodeParam.RGB) params).setBackgroundRGB(new int[]{1, 1, 1});

            baos = new ByteArrayOutputStream();
            ImageEncoder encoder = ImageCodec.createImageEncoder("PNG", baos, params);
            encoder.encode(image);
            baos.close();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        } catch (CDKException exc) {

            throw new CDKException(exc.toString());
        } catch (IOException e) {
            throw new CDKException("IO error" + e.toString());
        } catch (Exception e) {
            throw new CDKException("IO error" + e.toString());
        }

        return baos.toByteArray();
    }
}
