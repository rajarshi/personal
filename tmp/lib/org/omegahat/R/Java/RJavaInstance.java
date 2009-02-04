package org.omegahat.R.Java;

public class RJavaInstance 
    extends RForeignReference 
{
    boolean skipOmittedMethods = true;

    /**  Create a reference with the specified name.*/
    public RJavaInstance(String id) {
	super(id);
    }

    public RJavaInstance(org.omegahat.Interfaces.NativeInterface.ForeignReference id) {
	super(id);
    }

    public boolean getSkipOmittedMethods() {
	return(skipOmittedMethods);
    }

    public boolean setSkipOmittedMethods(boolean val) {
	skipOmittedMethods = val;
	return(skipOmittedMethods);
    }
}
