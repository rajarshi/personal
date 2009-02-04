///////////////////////////////////////////////////////////////////////////////
//  Filename: $RCSfile: SMILESExample.java,v $
//  Purpose:  Example for generating a molecule using SMILES.
//  Language: Java
//  Compiler: JDK 1.4
//  Authors:  Joerg Kurt Wegner
//  Version:  $Revision: 1.8 $
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
package joelib2.example;

import joelib2.io.BasicIOType;
import joelib2.io.BasicIOTypeHolder;
import joelib2.io.BasicMoleculeWriter;

import joelib2.molecule.BasicConformerMolecule;
import joelib2.molecule.Molecule;

import joelib2.smiles.SMILESParser;

import java.io.FileOutputStream;

import org.apache.log4j.Category;


/**
 * Example for generating a molecule using SMILES.
 *
 * @.author     wegnerj
 * @.license    GPL
 * @.cvsversion    $Revision: 1.8 $, $Date: 2005/02/17 16:48:29 $
 */
public class SMILESExample
{
    //~ Static fields/initializers /////////////////////////////////////////////

    // Obtain a suitable logger.
    private static Category logger = Category.getInstance(SMILESExample.class
            .getName());

    //~ Constructors ///////////////////////////////////////////////////////////

    public SMILESExample()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Initialize " + this.getClass().getName());
        }
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     *  The main program for the TestSmarts class
     *
     * @param  args  The command line arguments
     */
    public static void main(String[] args)
    {
        SMILESExample joeMolTest = new SMILESExample();

        if (args.length != 2)
        {
            joeMolTest.usage();
            System.exit(0);
        }
        else
        {
            Molecule mol = joeMolTest.test(args[0],
                    BasicIOTypeHolder.instance().getIOType("SDF"),
                    BasicIOTypeHolder.instance().getIOType("SDF"));
            joeMolTest.write(mol, args[1],
                BasicIOTypeHolder.instance().getIOType("SDF"));
        }

        System.exit(0);
    }

    /**
     *  A unit test for JUnit
     *
     * @param  molURL   Description of the Parameter
     * @param  inType   Description of the Parameter
     * @param  outType  Description of the Parameter
     */
    public Molecule test(String smiles, BasicIOType inType, BasicIOType outType)
    {
        Molecule mol = new BasicConformerMolecule(inType, outType);

        System.out.println("Generate molecule from \"" + smiles + "\"");

        // create molecule from SMILES string
        if (!SMILESParser.smiles2molecule(mol, smiles, "Name:" + smiles))
        {
            System.err.println("Molecule could not be generated from \"" +
                smiles + "\".");
        }

        //        Molecule mol1 = new Molecule(IOTypeHolder.instance().getIOType("SMILES"),IOTypeHolder.instance().getIOType("SMILES"));
        //        Molecule mol2 = new Molecule(IOTypeHolder.instance().getIOType("SMILES"),IOTypeHolder.instance().getIOType("SMILES"));
        //        joelib2.smiles.JOESmilesParser parser = new joelib2.smiles.JOESmilesParser();
        //        parser.smiToMol(mol1,"C0c1ccc(Cl)cc1Cl");
        //        parser.smiToMol(mol2,"C0c1ccc(Cl)cc1C");
        //
        //        System.out.print(mol1);
        //        System.out.print(mol2); // ERROR: terminal 'C' is converted to a Cl
        System.out.println(mol);
        mol.addHydrogens();
        System.out.println("Add hydrogens:");
        System.out.println(mol);

        return mol;
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
        sb.append("<SMILES pattern> <SDF file>");
        sb.append(
            "\n\nThis is version $Revision: 1.8 $ ($Date: 2005/02/17 16:48:29 $)\n");

        System.out.println(sb.toString());

        System.exit(0);
    }

    public void write(Molecule mol, String molFile, BasicIOType outType)
    {
        // create simple writer
        FileOutputStream fos = null;

        try
        {
            fos = new FileOutputStream(molFile);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.exit(1);
        }

        // write molecule to file
        try
        {
            BasicMoleculeWriter writer = new BasicMoleculeWriter(fos, outType);

            if (!writer.writeNext(mol))
            {
                System.err.println("Error writing SMILES.");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}

///////////////////////////////////////////////////////////////////////////////
//  END OF FILE.
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
//  END OF FILE.
///////////////////////////////////////////////////////////////////////////////
