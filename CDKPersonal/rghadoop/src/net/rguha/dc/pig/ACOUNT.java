package net.rguha.dc.pig;

import org.apache.pig.EvalFunc;
import org.apache.pig.impl.util.WrappedIOException;
import org.apache.pig.data.Tuple;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;

import java.io.IOException;

/**
 * Simple UDF to count atoms in a SMILES string.
 *
 * @author Rajarshi Guha
 */
public class ACOUNT extends EvalFunc <Integer> {
    static SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

    public Integer exec(Tuple tuple) throws IOException {
        if (tuple == null || tuple.size() == 0) return null;
        try {
            IAtomContainer mol = sp.parseSmiles((String) tuple.get(0));
            return mol.getAtomCount();
        } catch (InvalidSmilesException e) {
            throw WrappedIOException.wrap("Error parsing SMILES "+tuple.get(0), e);
        }
    }
}
