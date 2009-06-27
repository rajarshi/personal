package net.guha.apps.gui.wizard.stateui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;

/**
 * @author Rajarshi Guha
 */
public class StateGAFeatSelUI extends WizardStateUI {

    private static Log m_log = LogFactory.getLog(StateGAFeatSelUI.class);
    private int tabIteration;

    public StateGAFeatSelUI() {
        super();

        // panel = get a panel woth the UI
        panel = new JPanel();
    }


    public Object evaluate() {


        // set up a progress monitor for the ab insertion stage
//        int progressMonitorFactor = 20;
//        JLabel imgLabel = Misc.getImageLabel("com/pg/na/cadmol/modeling/wizard/images/gastage1.png", .9, .9);
//        ProgressMonitor tmpdlg;
//        if (imgLabel != null)
//            tmpdlg = new ProgressMonitor(null,
//                                         imgLabel, "Model 0",
//                                         0, nmodel * progressMonitorFactor);
//        else
//            tmpdlg = new ProgressMonitor(null,
//                                         "Inserting models", "Model 0",
//                                         0, nmodel * progressMonitorFactor);
//        final ProgressMonitor pgpdlg = tmpdlg;
//
//        pgpdlg.setMillisToDecideToPopup(0);
//        pgpdlg.setMillisToPopup(0);
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                if (tabIteration == pgpdlg.getMaximum()) pgpdlg.close();
//                else pgpdlg.setProgress(tabIteration);
//            }
//        });



        // lets make new tabs with each model in a seperate tab

//        for (int i = 0; i < nmodel; i++) {
//
//
//
//            pgpdlg.setProgress(tabIteration * progressMonitorFactor);
//            pgpdlg.setNote("Inserting model " + tabIteration);
//            if (pgpdlg.isCanceled()) {
//                return insertedObjects;
//            }
//
//
//        pgpdlg.close();

        return new Object();
    }


}
