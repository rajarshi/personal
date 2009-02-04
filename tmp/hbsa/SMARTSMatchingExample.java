///////////////////////////////////////////////////////////////////////////////
//  Filename: $RCSfile: SMARTSMatchingExample.java,v $
//  Purpose:  'SMiles ARbitrary Target Specification' (SMARTS) example for finding substructures.
//  Language: Java
//  Compiler: JDK 1.4
//  Authors:  Joerg Kurt Wegner
//  Version:  $Revision: 1.10 $
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

import joelib2.feature.types.atomlabel.AtomBondOrderSum;
import joelib2.feature.types.atomlabel.AtomExplicitHydrogenCount;
import joelib2.feature.types.atomlabel.AtomHeavyValence;
import joelib2.feature.types.atomlabel.AtomHybridisation;
import joelib2.feature.types.atomlabel.AtomImplicitHydrogenCount;
import joelib2.feature.types.atomlabel.AtomImplicitValence;
import joelib2.feature.types.atomlabel.AtomInAromaticSystem;
import joelib2.feature.types.atomlabel.AtomInRing;
import joelib2.feature.types.atomlabel.AtomInRingsCount;
import joelib2.feature.types.atomlabel.AtomIsElectronegative;
import joelib2.feature.types.atomlabel.AtomIsHydrogen;
import joelib2.feature.types.atomlabel.AtomKekuleBondOrderSum;
import joelib2.feature.types.bondlabel.BondInAromaticSystem;
import joelib2.feature.types.bondlabel.BondInRing;

import joelib2.io.BasicIOType;
import joelib2.io.BasicIOTypeHolder;
import joelib2.io.BasicReader;

import joelib2.molecule.Atom;
import joelib2.molecule.BasicConformerMolecule;
import joelib2.molecule.Molecule;

import joelib2.smarts.BasicSMARTSPatternMatcher;
import joelib2.smarts.SMARTSPatternMatcher;
import joelib2.smarts.SMARTSParser;

import wsi.ra.tool.BasicResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.util.List;

import org.apache.log4j.Category;
import wsi.ra.tool.BasicPropertyHolder;


/**
 *  'SMiles ARbitrary Target Specification' (SMARTS) example for finding
 *  substructures.
 *
 * @.author     wegnerj
 * @.license    GPL
 * @.cvsversion    $Revision: 1.10 $, $Date: 2005/02/17 16:48:29 $
 */
public class SMARTSMatchingExample
{
    //~ Static fields/initializers /////////////////////////////////////////////

    // Obtain a suitable logger.

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     *  The main program for the TestSmarts class
     *
     * @param  args  The command line arguments
     */
    public static void main(String[] args)
    {
 String value = BasicPropertyHolder.instance().getProperties()
                                          .getProperty(
                SMARTSParser.class.getName() +
                ".anyRecognizesExpliciteHydrogens");
System.out.println("XXXX "+value);

        SMARTSMatchingExample joeMolTest = new SMARTSMatchingExample();

        if (args.length != 2)
        {
            joeMolTest.usage();
            System.exit(0);
        }
        else
        {
            //        String molURL = new String("joelib/test/test.mol");
            joeMolTest.test(args[0], args[1],
                BasicIOTypeHolder.instance().getIOType("SDF"),
                BasicIOTypeHolder.instance().getIOType("SDF"));
        }

        System.exit(0);
    }

    /**
     *  A unit test for JUnit
     *
     * @param  molURL   Description of the Parameter
     * @param  smart    Description of the Parameter
     * @param  inType   Description of the Parameter
     * @param  outType  Description of the Parameter
     */
    public void test(String molURL, String smart, BasicIOType inType,
        BasicIOType outType)
    {
        // get molecules from resource URL
        byte[] bytes = BasicResourceLoader.instance()
                                          .getBytesFromResourceLocation(molURL);

        if (bytes == null)
        {
            System.exit(1);
        }

        ByteArrayInputStream sReader = new ByteArrayInputStream(bytes);

        // initialize SMART pattern
        SMARTSPatternMatcher sp = new BasicSMARTSPatternMatcher();
        System.out.println("... generate atom expression...");
        sp.init(smart);
        System.out.println(sp);

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
        System.out.println(" ... try to match: '" + smart + "' ...");

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

            // molecule for debugging purpose
            System.out.println(mol.toString(
                    BasicIOTypeHolder.instance().getIOType("SMILES")));

            List maplist;
            int[] itmp;

            if (sp.match(mol))
            {
                //maplist = sp.getMatches();
                maplist = sp.getMatches();

                if (maplist.size() > 0)
                {
                    System.out.println("Found pattern in " + mol.getTitle());
                }

                //print out the results
                for (int ii = 0; ii < maplist.size(); ii++)
                {
                    itmp = (int[]) maplist.get(ii);

                    for (int j = 0; j < itmp.length; j++)
                    {
                        System.out.print(itmp[j] + " ");
                    }

                    // show detailed atom properties for atom SMARTS pattern
                    if (itmp.length == 1)
                    {
                        Atom atom = mol.getAtom(itmp[0]);
                        System.out.print(" ImplicitValence:" +
                            AtomImplicitValence.getImplicitValence(atom));
                        System.out.print(" Valence:" + atom.getValence());

                        int hatoms =
                            AtomImplicitValence.getImplicitValence(atom) -
                            atom.getValence();
                        System.out.print(" Hydrogens:" + hatoms);
                    }

                    System.out.println("");
                }
            }
            else
            {
                System.out.println("Pattern NOT found in " + mol.getTitle());
            }
        }
    }

    /**
     *  Description of the Method
     */
    public void usage()
    {
        StringBuffer sb = new StringBuffer();
        String programName = this.getClass().getName();

        sb.append("\nUsage is : ");
        sb.append("java -cp . ");
        sb.append(programName);
        sb.append(" <SDF file> <SMARTS pattern>");
        sb.append(
            "\n\nThis is version $Revision: 1.10 $ ($Date: 2005/02/17 16:48:29 $)\n");

        System.out.println(sb.toString());

        System.exit(0);
    }
}

///////////////////////////////////////////////////////////////////////////////
//  END OF FILE.
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
//  END OF FILE.
///////////////////////////////////////////////////////////////////////////////
