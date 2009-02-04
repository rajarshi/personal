package org.omegahat.R.Java;

public class MissingMethodException 
         extends RuntimeException
{
    String methodName;

    public MissingMethodException(String msg) {
	super(msg);
    }

    public MissingMethodException(String method, Class source) {
	super("No method " + method + " in class " + source.getName());
        methodName = method;
    }

    public String getMethodName() {
	return(methodName);
    }
}
