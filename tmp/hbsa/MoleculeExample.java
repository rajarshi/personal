///////////////////////////////////////////////////////////////////////////////
//  Filename: $RCSfile: MoleculeExample.java,v $
//  Purpose:  Example for loading molecules and get atom properties.
//  Language: Java
//  Compiler: JDK 1.4
//  Authors:  Joerg Kurt Wegner
//  Version:  $Revision: 1.14 $
//            $Date: 2005/02/17 16:48:29 $
//            $Author: wegner $
//
// Copyright OELIB:          OpenEye Scientific Software, Santa Fe,
//                           U.S.A., 1999,2000,2001
// Copyright JOELIB/JOELib2: Dept. Computer Architecture, University of
//                           Tuebingen, Germany, 2001,2002,2003,2004,2005
// Copyright JOELIB/JOELib2: ALTANA PHARMA AG, Konstanz, Germany,
//                           2003,2004,2005
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation version 2 of the License.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
///////////////////////////////////////////////////////////////////////////////

import joelib2.data.BasicElementHolder;

import joelib2.feature.types.MolecularWeight;
import joelib2.feature.types.atomlabel.AtomBondOrderSum;
import joelib2.feature.types.atomlabel.AtomENPauling;
import joelib2.feature.types.atomlabel.AtomHybridisation;
import joelib2.feature.types.atomlabel.AtomImplicitValence;
import joelib2.feature.types.atomlabel.AtomInAromaticSystem;
import joelib2.feature.types.atomlabel.AtomPartialCharge;
import joelib2.feature.types.atomlabel.AtomType;
import joelib2.feature.types.bondlabel.BondIsClosure;

import joelib2.io.BasicIOType;
import joelib2.io.BasicIOTypeHolder;
import joelib2.io.BasicReader;

import joelib2.molecule.Atom;
import joelib2.molecule.BasicConformerMolecule;
import joelib2.molecule.Bond;
import joelib2.molecule.Molecule;

import joelib2.util.iterator.AtomIterator;
import joelib2.util.iterator.NbrAtomIterator;

import wsi.ra.tool.BasicResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.log4j.Category;


/**
 *  Example for loading molecules and get atom properties.
 *
 * @.author     wegnerj
 * @.license    GPL
 * @.cvsversion    $Revision: 1.14 $, $Date: 2005/02/17 16:48:29 $
 */
public class MoleculeExample
{
    //~ Static fields/initializers /////////////////////////////////////////////

    // Obtain a suitable logger.
    private static Category logger = Category.getInstance(MoleculeExample.class
            .getName());
    private static final boolean SHOW_ATOM_INFOS = true;

    //~ Instance fields ////////////////////////////////////////////////////////

    private StringBuffer buffer = new StringBuffer(10000);

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     *  The main program for the TestSmarts class
     *
     * @param  args  The command line arguments
     */
    public static void main(String[] args)
    {
        MoleculeExample joeMolTest = new MoleculeExample();

        if (args.length != 1)
        {
            joeMolTest.usage();
            System.exit(0);
        }
        else
        {
            joeMolTest.test(args[0],
                BasicIOTypeHolder.instance().getIOType("SDF"),
                BasicIOTypeHolder.instance().getIOType("SDF"));
        }

        joeMolTest.print2stdout();
    }

    /**
     *  A unit test for JUnit
     *
     * @param  molURL   Description of the Parameter
     * @param  inType   Description of the Parameter
     * @param  outType  Description of the Parameter
     */
    public void test(String molURL, BasicIOType inType, BasicIOType outType)
    {
        println("Supported molecule types:");
        println(BasicIOTypeHolder.instance().toString());

        // get molecules from resource URL
        byte[] bytes = BasicResourceLoader.instance()
                                          .getBytesFromResourceLocation(molURL);

        if (bytes == null)
        {
            logger.error("Molecule can't be loaded at \"" + molURL + "\".");
            System.exit(1);
        }

        ByteArrayInputStream sReader = new ByteArrayInputStream(bytes);

        // create simple reader
        BasicReader reader = null;

        try
        {
            reader = new BasicReader(sReader, inType);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        // load molecules and handle test
        Molecule mol = new BasicConformerMolecule(inType, outType);

        for (;;)
        {
            try
            {
                if (!reader.readNext(mol))
                {
                    break;
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                System.exit(1);
            }

            println("Original molecule:");
            println(mol);

            println("Add H atoms:");
            mol.addHydrogens(false, true, true);
            println(mol);

            println("Delete H atoms:");
            mol.deleteHydrogens();
            println(mol);
        }
    }

    /**
     *  Description of the Method
     */
    public void usage()
    {
        StringBuffer sb = new StringBuffer();
        String programName = this.getClass().getName();

        sb.append("Usage is : ");
        sb.append("java -cp . ");
        sb.append(programName);
        sb.append(" <SDF file>");
        sb.append(
            "\n\nThis is version $Revision: 1.14 $ ($Date: 2005/02/17 16:48:29 $)\n");

        println(sb.toString());

        System.exit(0);
    }

    /**
     * @param string
     */
    private void print(String string)
    {
        buffer.append(string);
    }

    /**
     *
     */
    private void print2stdout()
    {
        System.out.println(buffer.toString());
    }

    /**
     * @param mol
     */
    private void println(Molecule mol)
    {
        Atom atom;
        Atom nbr;
        AtomIterator ait = mol.atomIterator();
        NbrAtomIterator nait;
        int aromAtomsCounter = 0;

        while (ait.hasNext())
        {
            atom = ait.nextAtom();

            if (SHOW_ATOM_INFOS)
            {
                print("atom idx:" + atom.getIndex());
                print(", atomic number:" + atom.getAtomicNumber());
                print(", element symbol:" +
                    BasicElementHolder.instance().getSymbol(
                        atom.getAtomicNumber()));
                print(", internal atom type:" + AtomType.getAtomType(atom));

                if (AtomInAromaticSystem.isValue(atom))
                {
                    aromAtomsCounter++;
                }

                print(", aromatic flag:" + AtomInAromaticSystem.isValue(atom));
                print(", atom vector:" + atom.getCoords3D());
                print(", hybridisation:" + AtomHybridisation.getIntValue(atom));
                print(", implicit valence:" +
                    AtomImplicitValence.getImplicitValence(atom));
                print(", charge:" + atom.getFormalCharge());
                print(", partial charge:" +
                    AtomPartialCharge.getPartialCharge(atom));
                print(", valence:" + atom.getValence());
                print(", ext Electrons:" +
                    BasicElementHolder.instance().getExteriorElectrons(
                        atom.getAtomicNumber()));
                println(", bond order sum :" +
                    AtomBondOrderSum.getIntValue(atom));
                print(", pauling electronegativity:" +
                    AtomENPauling.getDoubleValue(atom));
                println(", free electrons:" + atom.getFreeElectrons());

                nait = atom.nbrAtomIterator();

                while (nait.hasNext())
                {
                    nbr = nait.nextNbrAtom();
                    println("  atom #" + atom.getIndex() +
                        " is attached to atom #" + nbr.getIndex() +
                        " with bond of order " +
                        nait.actualBond().getBondOrder());
                }
            }
        }

        for (int bondIdx = 0; bondIdx < mol.getBondsSize(); bondIdx++)
        {
            Bond bond = mol.getBond(bondIdx);
            println("Bond " + bondIdx + " (begin=" + bond.getBeginIndex() +
                ",end=" + bond.getEndIndex() + ") is " +
                (BondIsClosure.isClosure(bond) ? "a" : "not a") +
                " closure bond");
        }

        println("Molecule has " + aromAtomsCounter + " aromatic atoms.");
        println("Molecule weight:" + MolecularWeight.getMolecularWeight(mol));

        println(mol.toString());
    }

    /**
     * @param string
     */
    private void println(String string)
    {
        print(string);
        print("\n");
    }
}

///////////////////////////////////////////////////////////////////////////////
//  END OF FILE.
///////////////////////////////////////////////////////////////////////////////
