package net.guha.apps.gui.wizard;

import java.util.Stack;

/**
 * @author Rajarshi Guha
 */
public class WizardUndo extends Stack<Object> {


    public WizardUndo() {
        super();
    }

    /**
     * Undo all the operations performed so far.
     * <p/>
     * This implies that we will simply loop over the entire
     * undo stack and perform the appropriate undo operations.
     *
     * @param whichState The state that we are currently in
     */
    public void undoAll(int whichState) {
        if (!this.isEmpty()) {
            while (whichState >= 0) {
                undo(whichState);
                whichState--;
            }
        }
    }

    /*
    When we enter this method, we are currently *in* a state - that is it has not
    been executed. Thus we need to undo the previous state . Also each state of the
    wizard returns a List of objects inserted. Thus we should check that what we pop
    of the undo stack is a List object and then iterate over the List elements
    and perform the required undo action (which should be apparent from the type of
    the List element)
    */
    public void undo(int whichState) {
        Object insertedObjects;


//        switch (whichState) {
//            case WizardStates.STATE_LOGIN:
//                // nothing to undo before this state
//                break;
//            case WizardStates.STATE_SETS:
//                // nothing to really undo but since the init state
//                // did return a non-null value we need to pop it off the stack
//                this.pop();
//                break;
//            case WizardStates.STATE_DESC:
//                // do undo
//                Object insertedColumn = this.pop();
//                if (!(insertedColumn instanceof Column)) {
//                    throw new IllegalArgumentException();
//                }
//                dataModel.removeColumn((Column) insertedColumn);
//                break;
//            case WizardStates.STATE_REDU:
//                // do undo
//                Object insertedColumns = this.pop();
//                if (!(insertedColumns instanceof ArrayList)) {
//                    throw new IllegalArgumentException();
//                }
//
//                for (Iterator iter = ((ArrayList) insertedColumns).iterator(); iter.hasNext();) {
//                    Object column = iter.next();
//                    if (!(column instanceof Column)) {
//                        throw new IllegalArgumentException();
//                    }
//                    dataModel.removeColumn((Column) column);
//                }
//                break;
//            case WizardStates.STATE_FSEL:
//                insertedObjects = this.pop();
//                if (!(insertedObjects instanceof ArrayList)) {
//                    throw new IllegalArgumentException();
//                }
//                for (Iterator iter = ((ArrayList) insertedObjects).iterator(); iter.hasNext();) {
//                    Object object = iter.next();
//                    if (object instanceof PLWorksheet) {
//                        ProjectWindow projectWindow = (ProjectWindow) ProjectLeader.getInstance().getSelectedFrame();
//                        projectWindow.removeTab((PLWorksheet) object);
//                    } else if (object instanceof Column) {
//                        dataModel.removeColumn((Column) object);
//                    } else if (object instanceof WizardReportPage) {
//                        WizardReportPage.getInstance().popChunk();
//                    }
//                }
//
//                break;
//            case WizardStates.STATE_END:
//                insertedObjects = this.pop();
//                if (!(insertedObjects instanceof ArrayList)) {
//                    throw new IllegalArgumentException();
//                }
//                for (Iterator iter = ((ArrayList) insertedObjects).iterator(); iter.hasNext();) {
//                    Object object = iter.next();
//                    if (object instanceof PLWorksheet) {
//                        ProjectWindow projectWindow = (ProjectWindow) ProjectLeader.getInstance().getSelectedFrame();
//                        projectWindow.removeTab((PLWorksheet) object);
//                    } else if (object instanceof WizardReportPage) {
//                        WizardReportPage.getInstance().popChunk();
//                    }
//                }
//                break;
//        } // end switch
    } // end undo
}
