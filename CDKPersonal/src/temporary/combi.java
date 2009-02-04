package temporary;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author rguha
 */
public class combi {
    SmilesParser sp;
    SmilesGenerator sg;

    public combi() {
        sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        sg = new SmilesGenerator();
    }


    public String getFragment(String smiles, String group, int closureNumber) throws CDKException {
        ArrayList x = new ArrayList();
        IAtom replacement = DefaultChemObjectBuilder.getInstance().newPseudoAtom();
        replacement.setAtomicNumber(99999);

        // parse the molecule
        IAtomContainer molecule = sp.parseSmiles(smiles);

        // set up the pattern matching
        SMARTSQueryTool sqt = new SMARTSQueryTool(group, true);

        boolean status = sqt.matches(molecule);
        if (!status) throw new CDKException("The given group was not found");

        // ok, so we've found a match. Let's make sure that its
        // a single match and if so get the mappings
        int nmatch = sqt.countMatches();
        if (nmatch != 1) throw new CDKException("More than one match found. Skipping");
        List atomIndices = (List) sqt.getMatchingAtoms().get(0);

        IAtom attachmentAtom = null;

        // go through the matched atoms and see which connects the group
        // to the rest of the molecule. Basically find the atom at the end of
        // a bond from one of the matched atoms that is not in the list of
        // matched atoms
        for (Object o1 : atomIndices) {
            int index = (Integer) o1;
            List<IAtom> connectedAtoms = molecule.getConnectedAtomsList(molecule.getAtom(index));
            for (Object o : connectedAtoms) {
                IAtom atom = (IAtom) o;
                if (!atomIndices.contains(molecule.getAtomNumber(atom))) {
                    attachmentAtom = atom;
                    break;
                }
            }
        }

        // now delete all the atoms we matched, and their bonds. First get
        // the atom's themselves since as we remove atoms, the indices will change
        List<IAtom> tmp = new ArrayList<IAtom>();
        for (Object o1 : atomIndices) {
            int index = (Integer) o1;
            IAtom atom = molecule.getAtom(index);
            tmp.add(atom);
        }
        for (IAtom o : tmp) {
            System.out.println("o.getSymbol() = " + o.getSymbol());
            molecule.removeAtomAndConnectedElectronContainers(o);
        }

        // in some cases me way end up no atoms, eg: replacing COOH in HCOOH
        // just leaves a H, which is implicit and so the atom container is empty
        // if so, we add an explicit hydrogen and make this the attachment atom
        if (molecule.getAtomCount() == 0) {
            IAtom hydrogen = DefaultChemObjectBuilder.getInstance().newAtom("H");
            attachmentAtom = hydrogen;
            molecule.addAtom(hydrogen);
        }

        // now add the replacement atom to the attachment point we identified above
        molecule.addAtom(replacement);
        IBond bond = DefaultChemObjectBuilder.getInstance().newBond(replacement, attachmentAtom);
        molecule.addBond(bond);

        // now lets get the SMILES for the fragment
        String fragSmiles = sg.createSMILES((IMolecule) molecule);
        fragSmiles = fragSmiles.replaceAll("\\[\\*]", "%" + closureNumber);

        return fragSmiles;
    }

    public static void main(String[] args) throws IOException, CDKException {
        String line;
        String[] amides = {"NC", "NCC", "NC#C", "CC(C)N"};
        String[] aldehydes = {"O=Cc1ccccc1", "O=Cc1ccccc1C", "C(C1=CC(OC)=C(OC)C=C1)=O"};
        String[] acids = {

                "CC(C)(C)OC(=O)NCC(O)=O",
                "CC(C)(C)OC(=O)NC(C)C(O)=O"
        };

        combi c = new combi();
        String f = c.getFragment(aldehydes[2], "[CX3h1]=O", 92);
        System.out.println("f = " + f);
        System.exit(0);

        BufferedReader reader = new BufferedReader(new FileReader("/home/rguha/src/study/ugi/aldehydes02.txt"));
        while((line = reader.readLine()) != null) {
            line = line.trim();
            String frag =   null;
            try {
                frag = c.getFragment(line, "C(O)=O", 92);
            } catch (CDKException e) {
                System.out.println("Error processing: " + line+" "+e);
                continue;
            }
            System.out.println(line + " --> " + frag);
        }
        /*
        for (String anAmide : amides) {
            System.out.println(c.getFragment(anAmide, "N", "90"));
        }

        System.out.println("----");
        for (String anAldehyde : aldehydes) {
            System.out.println(c.getFragment(anAldehyde, "C=O", "91"));
        }

        System.out.println("----");

        for (String anAcid : acids) {
            System.out.println(anAcid + " --> " + c.getFragment(anAcid, "C([Oh1])=O", "92"));
        }
        */

    }
}
