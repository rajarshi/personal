package net.rguha.dc.pig;

import org.apache.pig.FilterFunc;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.util.WrappedIOException;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import java.io.IOException;

/**
 * Simple UDF to perform a SMARTS match.
 *
 * Example usage would be
 * <code>
 * A = load 'molecules.smi' as (smiles:chararray);
 * B = filter A by net.rguha.dc.pig.SMATCH(smiles, 'NC(=O)C(=O)N');
 * </code>
 *
 * @author Rajarshi Guha
 */
public class SMATCH extends FilterFunc {
    static SMARTSQueryTool sqt;static {
        try {
            sqt = new SMARTSQueryTool("C");
        } catch (CDKException e) {
            System.out.println(e);
        }
    }
    static SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

    public Boolean exec(Tuple tuple) throws IOException {
        if (tuple == null || tuple.size() < 2) return false;
        String target = (String) tuple.get(0);
        String query = (String) tuple.get(1);
        try {
            sqt.setSmarts(query);
            IAtomContainer mol = sp.parseSmiles(target);
            return sqt.matches(mol);
        } catch (CDKException e) {
            throw WrappedIOException.wrap("Error in SMARTS pattern or SMILES string "+query, e);
        }
    }
}
