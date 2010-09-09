package net.rguha.dc.pig;

import org.apache.pig.FilterFunc;
import org.apache.pig.data.Tuple;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.exception.CDKException;

import java.io.IOException;

/**
 * Simple UDF to perform a SMARTS match.
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

    public Boolean exec(Tuple tuple) throws IOException {
        if (tuple == null || tuple.size() == 0) return false;
        
        return false;
    }
}
