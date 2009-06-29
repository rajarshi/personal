package net.guha.apps.gui.wizard;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;


/**
 * A JComboBox that filters elements as the user types.
 *
 * Filtering is done based on whether the typed text is
 * contained within the list items (not just at the beggining).
 * Taken from http://forums.java.net/jive/message.jspa?messageID=295731
 * and seems to be a little crude. But does the job for now.
 */
public class FilteringComboBox extends JComboBox {
    private Vector<String> items;   

    public FilteringComboBox(Vector<String> objects) {
        super(objects);

        items = new Vector<String>(objects);

        setEditable(true);
        getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() != 38 && e.getKeyCode() != 40 && e.getKeyCode() != 10) {
                    String a = getEditor().getItem().toString();
                    removeAllItems();
                    boolean showPopup = false;

                    for (String item : items) {
                        if (item.indexOf(a) != -1) {
                            addItem(item);
                            showPopup = true;
                        }
                    }
                    getEditor().setItem(a);
                    JTextField textField = (JTextField) e.getSource();
                    textField.setCaretPosition(textField.getDocument().getLength());
                    hidePopup();
                    if (showPopup) {
                        showPopup();
                    }
                }
            }
        });

        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (getSelectedIndex() == -1) {
                }
            }
        });
    }
}
