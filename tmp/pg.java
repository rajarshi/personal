import java.sql.*;

import org.openscience.cdk.qsar.model.R.LinearRegressionModel;
import org.openscience.cdk.qsar.model.R.LinearRegressionModelSummary;
import org.openscience.cdk.qsar.model.R.CNNClassificationModel;
import org.openscience.cdk.qsar.model.QSARModelException;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.qsar.DescriptorEngine;
import org.openscience.cdk.qsar.Descriptor;
import org.openscience.cdk.qsar.DescriptorSpecification;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.result.*;
import org.openscience.cdk.AtomContainer;
import java.lang.ClassLoader;

import java.util.Vector;
import java.util.List;

import util.misc;
public class pg {
    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("Loaded PostgreSQL JDBC driver");

            try {
                Connection db = DriverManager.getConnection("jdbc:postgresql://white.chem.psu.edu/qsarDB","qsar","f1cklem1ckle");
                System.out.println("Got connection to white");

                Statement st = db.createStatement();

                System.out.println("Now getting the BP model");
                ResultSet rs = st.executeQuery("SELECT * FROM models WHERE modelname = 'bp'");
                while(rs.next()) {
                    System.out.println(rs.getString(1)+" | "+rs.getString(2));
                    String modelString = rs.getString(4);
                    System.out.println("Now converting model string to R model object");
                    LinearRegressionModel lrm = new LinearRegressionModel();
                    lrm.loadModel(modelString, "BPModel");
                    LinearRegressionModelSummary lrms = lrm.summary();
                    System.out.println("Adj R^2 = "+lrms.getAdjRSQ());
                    System.out.println("F-value = "+lrms.getFStatistic());

                    Double[][] newx = { 
                        {new Double(1),
                        new Double(2),
                        new Double(3),
                        new Double(4),
                        new Double(5)},
                    {new Double(1),
                        new Double(2),
                        new Double(3),
                        new Double(4),
                        new Double(5)}

                    };
                    lrm.setParameters("newdata", newx);
                    lrm.setParameters("interval","confidence");
                    lrm.predict();
                    System.out.println("Predicted = "+lrm.getPredictPredicted()[0]);

                    System.out.println("\nProcessing descriptor list");
                    String[] desclist = rs.getString(5).split("#");

                    DescriptorEngine engine = new DescriptorEngine();

                    Vector v = misc.loadMolecules(new String[] { "mols/dan007.hin" }, true);
                    AtomContainer ac = (AtomContainer)v.get(0);

                    double[][] data = new double[1][desclist.length];
                    
                    for (int  i = 0; i < desclist.length; i++) {
                        System.out.println(desclist[i]);
                        String[] tmp = desclist[i].split("\\.");
                        String name = tmp[0];
                        int whichone = 0;
                        if (tmp.length == 2) {
                            whichone = Integer.decode(tmp[1]).intValue();
                        } else whichone = -1;
                        System.out.println("  "+name+"  "+whichone);
                        Class dc = Class.forName("org.openscience.cdk.qsar."+name);

                        DescriptorResult retval = null;
                        try {
                            Descriptor d = (Descriptor)dc.newInstance();
                            retval = d.calculate(ac).getValue();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (CDKException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        if (retval instanceof DoubleArrayResult) {
                            data[0][i] = ((DoubleArrayResult)retval).get(whichone);
                        } else if (retval instanceof DoubleResult) {
                            data[0][i] = ((DoubleResult)retval).doubleValue();
                        } else if (retval instanceof IntegerResult) {
                            data[0][i] = (double)((IntegerResult)retval).intValue();
                        } else if (retval instanceof IntegerArrayResult) {
                            data[0][i] = (double)((IntegerArrayResult)retval).get(whichone);
                        }
                    }
                    for (int i = 0; i < desclist.length; i++) {
                        System.out.println(data[0][i]);
                    }
                    System.out.println("\n\n");
                }
                System.out.println("\n\nNow getting the validation model");
                rs = st.executeQuery("SELECT * FROM validmodels WHERE modelname = 'bp'");
                while(rs.next()) {
                    String modelString = rs.getString(4);
                    System.out.println("Now converting valid model string to R model object");
                    CNNClassificationModel ccm = new CNNClassificationModel();
                    ccm.loadModel(modelString, "BPVModel");
                    System.out.println("Fit value = "+ ccm.getFitValue());

                    Double[][] newx = { 
                        {new Double(1),
                        new Double(2),
                        new Double(3),
                        new Double(4),
                        new Double(5)},
                    {new Double(1),
                        new Double(2),
                        new Double(3),
                        new Double(4),
                        new Double(5)}

                    };
                    ccm.setParameters("newdata", newx);
                    ccm.setParameters("type", "raw");
                    ccm.predict();
                    double[][] preds = ccm.getPredictPredictedRaw();
                    System.out.println("Predicted = "+preds[0][0]);
                }

                
            } catch (SQLException sqle) {
                System.out.println(sqle.getErrorCode());
                System.out.println(sqle.getSQLState());
                sqle.printStackTrace();
            } catch (QSARModelException e) {
                System.out.println(e.toString());
            }
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }


    }
}
