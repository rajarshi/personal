
package org.omegahat.R.Java;

import java.util.Hashtable;


/**
 Provides facilities for  evaluating R expression and calling
 R functions from within Java.
 This acts as a front end to the R interpeter or evaluator.
 One can evaluate R commands given as strings or, more
 flexibly, invoke  R functions passing them Java objects
 as arguments.
 In the future, there will be multiple, separate R evaluators
 and different instances of this Java class will correspond to 
 different R interpreters.
*/

public class REvaluator
        extends RForeignReference
{
/**
 Default constructor, simply creating an unamed reference to itself.
*/
public REvaluator() 
{
 super("-1");
}

static public String Monitor = "";

static native public boolean  lockEvaluator(boolean on);

/**
 Evaluate the specified S command, given as a string
 and return the result, having converted it from S to Java.
*/
public Object eval(String cmd)
{
 return(eval(cmd, null, true));
}    

/**
  Evaluate the specified S command and discard the result,
 avoiding the conversion to Java.
*/
public Object voidEval(String cmd)
{
 return(eval(cmd, false, (Class)null, true));
}    

/**
 Evaluate the specified S command, identifying
 the type of the return value expected.
*/
public Object eval(String cmd, Class returnClass)
{
 return(eval(cmd, returnClass, true));
}

/**
 Evaluate the specified S command and control whether the
 result in S is converted to Java or returned
 as a reference in Java to that S object.
 @param convert if <code>true</code>, the S value is 
  converted using any registered converters.
  Alternatively, if it is <code>false</code>, the
  S value is stored in S and a Java reference to it
  is returned from this call. That reference 
  is typically an object of class
  {@link org.omegahat.R.Java.RForeignReference RForeignReference}
  or some derived class and can be used in subsequent computations
  with some or all of its methods accessing the associated S object.
*/
public Object eval(String cmd, boolean convert)
{
 return(eval(cmd, (Class)null, true));
}

/**
 The complete method for evaluating an S command given as a string,
 allowing control of whether the result is converted to Java 
 or returned as a reference, and also supporting the specification
 of a return type from the evaluation which controls to what 
 Java type  the result is converted.

*/
public Object eval(String cmd, Class returnClass, boolean convert)
{
 return(eval(cmd, true, returnClass, false));
}    


/**
 A native C method that evaluates an S command given as a string
 which is used to bridge the interface between Java and S.
*/
native public Object eval(String cmd, boolean convert, Class returnClass, boolean discardResult);


static public void
yieldThread()
{
  Thread.currentThread().yield();
}  



/**
 Call an S function with no arguments.

 @param name the name of the S function to call.
  This is found by looking for it in the top-level R search path.
*/
public Object call(String name) {
 return(call(name, (Object[])null, (String[])null, (Class)null));
}

/**
 Call an S function with the arguments given in the 
  array of values.
 @param name the name of the S function to call, found in the
  typical fashion in the R search path.
 @param args an array of Java objects which are to be converted
 to S objects and passed as arguments to the S function.
*/
public Object call(String name, Object[] args) {
 return(call(name, args, new String[args.length]));
}

/**
  Supports specification of the convert argument.
  */
public Object call(String name, Object[] args, boolean convert) {
 return(call(name, args, new String[args.length], convert));
}

/**
 Call the named S function with the specified argument values
 and the names of the arguments specified <code>argName</code>,
 which can contain <code>null</code> values for un-named arguments.
*/
public Object call(String name, Object[] args, String[] argNames) {
 return(call(name, args, argNames, null));
}

/**
  Supports specification of the convert argument.
  */
public Object call(String name, Object[] args, String[] argNames, boolean convert) {
 return(call(name, args, argNames, (Class) null, convert));    
}

/**
 Call the named S function with the specified argument values
 and the names of the arguments specified <code>argName</code>,
 which can contain <code>null</code> values for un-named arguments
 and specifying the class to which the return value should be converted.
*/
public Object call(String name, Object[] args, String[] argNames, Class toClass) {
 return(call(name, args, argNames, toClass, true));
}

/**
  The native method that actually performs the call to the S function
 given the arguments and their names and the target class for the result.

 @param convert a logical value indicating whether we should attempt to convert the result
   to a Java object or simply leave it as a <a href="ForeignReference">foreign reference</a>. 
*/
protected native Object call(String name, Object[] args, String[] argNames, Class toClass, boolean convert);


/*
 A method to call an S function that takes unnamed, ordered arguments and then
 a collection of named arguments, e.g a call like
     foo(1,"a", value=1, z=2)
 
*/
public Object call(String name, Object[] args, Hashtable namedArgs) {
 String[] argNames;
 if(args == null) {
   argNames = new String[namedArgs.size()];
   args = createArgs(namedArgs, argNames);
 } else {
  int totalNumArgs = args.length + namedArgs.size();
  argNames = new String[totalNumArgs];
  Object[] newArgs = new Object[totalNumArgs];
   System.arraycopy(args, 0, newArgs, 0, args.length);
   args = createArgs(namedArgs, argNames, newArgs, args.length);
 }

 return(call(name, args, argNames, null));
}

/**
 Call an S function with only named arguments, where the 
 name-value pairs come from the given {@link java.util.Hashtable Hashtable}.
*/
public Object call(String name, Hashtable namedArgs) {
 String[] names;
  names = new String[namedArgs.size()];
 Object[] args =  createArgs(namedArgs, names);
 return(call(name, namedArgs));
}

/**
  Unimplemented!
*/
public Object call(String name, org.omegahat.Environment.Utils.OrderedTable args, Class toClass) {
 return(call(name, args, toClass, true));
}

public Object call(String name, org.omegahat.Environment.Utils.OrderedTable args) {
 return(call(name, args, (Class) null, true));
}

public Object call(String name, org.omegahat.Environment.Utils.OrderedTable args, Class toClass, boolean convert)
{

  Object[] a = new Object[args.size()];
  String[] names;
  java.util.Vector v;  
  v = args.orderedKeys();

  names = new String[v.size()];
  for(int i = 0; i < v.size(); i++) {
    Object tmp = v.elementAt(i);
    if(tmp instanceof String)
     names[i] = (String) tmp;
  }

  return(call(name, args.ordered().toArray(), names, toClass, convert));
// throw new RuntimeException("Unimplemented");
}

protected Object[] createArgs(java.util.Hashtable namedArgs, String[] names)
{
 Object[] args = new Object[namedArgs.size()];

 createArgs(namedArgs, names, args, 0);
 return(args);
}

protected Object[] createArgs(java.util.Hashtable namedArgs, String[] names, Object[] args, int offset)
{ 
  String key;
  int n = namedArgs.size();
  java.util.Enumeration e;
    e = namedArgs.keys();
  for(int i = 0; i < n; i++) {
    names[i + offset] = key = (String)e.nextElement();
    args[i+offset] = namedArgs.get(key);
  }

 return(args);
}

}

/*
Copyright (c) 1998 1999, The Omega Project for Statistical Computing.
     All rights reserved.

     Redistribution and use in source and binary forms, with or without
     modification, are permitted provided that the following conditions are
     met:

       Redistributions of source code must retain the above copyright notice,
       this list of conditions and the following disclaimer.

       Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

       Neither name of the Omega Project for Statistical Computing nor the 
       names of its contributors may be used to endorse or promote products 
       derived from this software without specific prior written permission.

     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS'' AND ANY
     EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
     MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
     SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
     SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
     PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
     INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
     STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
     THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 

*/


