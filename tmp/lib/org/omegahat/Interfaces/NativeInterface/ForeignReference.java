
package org.omegahat.Interfaces.NativeInterface;



/**
    An abstract class that implements the basic functionality for
    converting a foreign reference (i.e. an object exported from R or S)
    to an appropriate Java object.
    This contains information identifying the R/S object and the target
    Java class. If the class does not exist (or is not likely to) the
    object can be initialized with the names of classes and interfaces
    which a dynamically generated class should extend and implement, respectively 
    The foreign reference is then converted to an instance of this class
    so that it can be used appropriately. This new instance implements its methods 
    via calls back to the foreign system and methods for the specified remote object.

    This is used as a transfer agent between the foreign system and Omegahat/Java.
    The foreign system creates an object of this class via C routines and then passes
    this object to the Omegahat system as an argument in a method call.
  */
//abstract 
public class ForeignReference
                   implements ForeignReferenceInt
{
/**
 The identifier of the object in the foreign system.
*/
protected String name;

protected  OmegaInterfaceManager evaluator;

public ForeignReference(String id) {
 setReferenceName(id);
}

public ForeignReference(String id, OmegaInterfaceManager evaluator) {
 this(id);
 setEvaluator(evaluator);
}

public ForeignReference(ForeignReference ref) {
 this(ref.getReferenceName());
}

public ForeignReference(ForeignReference ref, OmegaInterfaceManager evaluator) {
 this(ref.getReferenceName(), evaluator);
}


/**
 Iterates over the specified arguments and processes each one so it can be passed
 to the native method which will call the foreign system - R or S - converting
 non-convertable objects to anonymous references.

*/
protected Object[] processArgs(org.omegahat.Environment.Parser.Parse.List args)
          throws Exception
{
  Object []ans = null;
  int n = args.size();
  if(n > 0) {
    Object tmp;
    ans = new Object[n];
    for(int i = 0; i < n ; i++) {
       tmp = args.elementAt(i);
       ans[i] = processArg(tmp);
    }
  }

 return(ans);
}

/**
  Convert an argument to an anonymous reference, or leave as is.
 */
protected Object processArg(Object arg)
           throws Exception
{

 if(getEvaluator().convertable(arg) == false) {
    arg = evaluator.anonymousAssign(arg);
 }
  
 return(arg);
}

/**
 Used to clean up any anonymous references created by {@link processArgs(org.omegahat.Environment.Parser.Parse.List) processArgs()}
 before the call to the foreign system.
*/
protected int removeTemporaryAnonymousReferences(Object[] args, org.omegahat.Environment.Parser.Parse.List l)
{
 int n = args != null ? args.length : 0;
 int ctr = 0;

 for(int i = 0; i < n; i++) {
   if(l.elementAt(i) != args[i]) {
      ctr++;
   }
 }

 return(ctr);
}

/**
  The method that performs the callback to the foreign system and arranges to resolve the reference,
  convert the arguments, invoke the appropriate method identified by <code>methodName</code>, 
  convert the return value, etc. 
  This is currently implemented as a dummy version that prints its information.
  It is left up to the sub-classes to perform the actual 
  call to the host system (R or S).
*/
public Object eval(org.omegahat.Environment.Parser.Parse.List args, String methodName, String returnClass, String []jsignature)
          throws Exception
{
 System.err.println("Foreign method invocation on " + getReferenceName());
 System.err.println("# args " + (args != null ? args.size() : 0));
 System.err.println("Would invoke foreign method " + methodName + " expecting " + returnClass);

 if(jsignature != null) {
   for(int i = 0; i < jsignature.length; i++)
     System.err.println(i + ") " + jsignature[i]);
 }

 return(this);
}

/**
This is used to convert the object the foreign system (e.g R/S) gives us when we call
a function in that system from a dynamically generated Java method
(e.g. via the method {@link org.omegahat.R.Java.RForeignReference#eval(org.omegahat.Environment.Parser.Parse.List,string,String,String[]) eval(List, String, String, String[])}.
  */
public Object convertResult(Object value, String type)
         throws Throwable
{
//System.err.println("Converting " + value + " to type " + type);

 Object result = value;
 if(type.equals("long")) {
   if(value instanceof Number) {
     result = new Long( ((Number) value).longValue());
   } else if(value instanceof String) {
     result = new Long( (String) value);
   }
 } else if(type.equals("float")) {
   if(value instanceof Number) {
     result = new Float( ((Number) value).longValue());
   } else if(value instanceof String) {
     result = new Float( (String) value);
   }
 } else if(type.equals("boolean")) {
   if(value instanceof Number) {
     result = new Boolean( ((Number) value).intValue() != 0);
   } else if(value instanceof String) {
     result = new Boolean( (String) value);
   }
 } else if(type.equals("int")) {
   if(value instanceof Number) {
     result = new Integer( ((Number) value).intValue());
   } else if(value instanceof String) {
     result = new Integer( (String) value);
   }
 } else if(type.equals("double")) {
   if(value instanceof Number) {
     result = new Double( ((Number) value).doubleValue());
   } else if(value instanceof String) {
     result = new Double( (String) value);
   }
 } else if(type.equals("char")) {
   if(value instanceof String) {
     result = new Character( ((String) value).charAt(0));
   }
 } else if(value instanceof InterfaceReference) {
      result = ((AnonymousReference)value).eval(getEvaluator());
 }
 


 return(result);
}  


/**
   Accessor for {@link #name name} field
 */
public String getReferenceName()
{
   return(name);
}


/**
   Accessor for {@link #name name} field
 */
public String setReferenceName(String value)
{
  name = value;
   return(getReferenceName());
}


/**
   Accessor for {@link #evaluator evaluator} field.
   This can now compute its evaluator if it has not been
   previously set.
   @see org.omegahat.Environment.System.Globals#evaluator()
 */
public OmegaInterfaceManager getEvaluator()
{
  if(evaluator == null) {
   try {
//  evaluator = (OmegaInterfaceManager) org.omegahat.Environment.System.Globals.manager().evaluator();
    evaluator = (OmegaInterfaceManager) org.omegahat.Environment.System.Globals.evaluator();
   } catch(ClassNotFoundException ex) {
      ex.printStackTrace();
   }
  }
   return(evaluator);
}

/**
   Accessor for setting {@link #evaluator evaluator} field
 */

public OmegaInterfaceManager setEvaluator(OmegaInterfaceManager value)
{
   evaluator = value;
  return(evaluator);
}

public String toString() 
{
  return(getClass().getName() + ": id " +  getReferenceName());
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

