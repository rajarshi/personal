import javax.swing.*;
import java.io.*;

public class x {

    public x() {
    JFrame f = new JFrame("Hi there");
    String s = "<html><body><h2>HTML Editor pane</h2>"+
        "<table border=5><tr><td>Hi</td><td>There</td></tr></table>"+
        "<body></html>";
    JEditorPane jep= null;
    
        jep = new JEditorPane("text/html",s);
    jep.setEditable(false);

    f.pack();
    f.show();
    
    JOptionPane.showMessageDialog(
            f,
            new JScrollPane(jep),
            "A Message",
            JOptionPane.ERROR_MESSAGE
            );
    }
    public static void main(String[] args) {
        x xx = new x();
    }
}
    

    
