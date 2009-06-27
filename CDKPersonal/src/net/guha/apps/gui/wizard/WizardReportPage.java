/**
 * 
 * @author Rajarshi Guha
 */
package net.guha.apps.gui.wizard;


import javax.swing.*;
import java.util.ArrayList;

public class WizardReportPage {

    private ArrayList<String> chunks;
    private JEditorPane editorPane = null;

    private static WizardReportPage ourInstance = new WizardReportPage();

    public static WizardReportPage getInstance() {
        return ourInstance;
    }

    public boolean isEmpty() {
        return chunks.size() == 0;
    }

    public void pushChunk(String text) {
        chunks.add(text);
    }

    public JEditorPane getWizardPane() {
        if (editorPane == null) {
            editorPane = new JEditorPane("text/html", generatePage());
        }
        return editorPane;
    }

    public String popChunk() {
        int nchunk = chunks.size();
        if (nchunk == 0) return null;
        else
            return chunks.remove(nchunk - 1);
    }

    public String generatePage() {
        StringBuffer stringBuffer = new StringBuffer();
        return stringBuffer.toString();
    }

    public void clearPage() {
        chunks.clear();
    }

    private WizardReportPage() {
        chunks = new ArrayList<String>();
    }
}
