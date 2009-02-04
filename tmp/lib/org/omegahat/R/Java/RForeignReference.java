
package org.omegahat.R.Java;



/**
    This is the main class for representing an S object in Java,
    where the S object is not copied to Java but stored within S
    and accessed in Java via this "proxy" reference.
    It is the base class for representing  a Foreign Reference
    <i>after</i> it has been converted from a
    {@link org.omegahat.Interfaces.NativeInterface.EnvironmentForeignReference ForeignReference }
    This instantiation occurs when the {@link org.omegahat.Interfaces.NativeInterface.ForeignReference ForeignReference}
    is evaluated, and potentially a new class  is generated. Then an object of the necessary
    type that is derived from this class  is created. 
  */
public class RForeignReference
        extends org.omegahat.Interfaces.NativeInterface.ForeignReference
{ 
static boolean LoadLibrary = true;
static {
if(LoadLibrary) {
 try {
  // System.err.println("Loading native methods from SJava library");
  System.loadLibrary("SJava");
 } catch(Exception ex) { 
     System.err.println(ex);
     ex.printStackTrace(); 
  }
 }
}

/**
  Create a reference with the specified name.
*/
public RForeignReference(String id) {
 super(id);
}

/**
 Create a reference with the specified identifier/name an 
 register that its Omegahat evaluator is the given here.
 This is useful when there is more than one Omegahat
 evaluator and we want the methods of this reference to be
 evaluated within this Omegahat interpreter.
*/
public RForeignReference(String id, org.omegahat.Interfaces.NativeInterface.OmegaInterfaceManager evaluator) {
 super(id, evaluator);
}

public RForeignReference(org.omegahat.Interfaces.NativeInterface.ForeignReference id) {
 super(id);
}

public RForeignReference(org.omegahat.Interfaces.NativeInterface.ForeignReference id, org.omegahat.Interfaces.NativeInterface.OmegaInterfaceManager evaluator) {
 super(id, evaluator);
}


/**
 The method that performs the call back to R to invoke the method named
  <code>methodName</code> on the object identified by this objects
  reference identifier ({@link #getReferenceName() getReferenceName()}).
  This then attempts to convert the answer R gave us to the one required by
  the method call that invoked this using
  {@link #convertResult(Object,String) convertResult}.
*/
public Object localEval( org.omegahat.Environment.Parser.Parse.List args, String methodName, String returnClass, String []jsignature)
        throws Exception
{
  Object[] convertedArgs = processArgs(args);

  Object obj = reval(getReferenceName(), convertedArgs, methodName, returnClass, jsignature);

   try {
     obj = convertResult(obj, returnClass);
   } catch(Throwable e) {
       throw new RuntimeException("The R method "+methodName+" returned an object of the wrong type " + obj.getClass() + " expected " + returnClass) ;
   }       
 return(obj);
}


/**
 Native method that invokes the "method" identified by <code>methodName</code>
 on the underlying S object (usually a closure), passing it the arguments
 given in the array <code>args</code>. 
 
 This is usually called as the single entry point that allows Java to 
 invoke methods in an object that is actually stored in S.
 So within a single Java class that is a reference to an S object, 
 we may have multiple (possibly overloaded) methods and 
 we implement each of these via a call to reval().
 These pass their own signature with which they are defined,
 including the types of the arguments and the return type.
 Usually, these classes and their methods are generated
  programmatically via the byte-code compiler provided by
 Omegahat, {@link org.omegahat.Interfaces.NativeInterface.ForeignReferenceClassGenerator
 ForeignReferenceClassGenerator}.
 
 @param the identifier for this reference object
 @param args the array of arguments to pass to the S method
 @param methodName the name of the "method" within the S object to
 invoke 
 @param returnClass the name of the return type expected.
*/
native public Object reval(String id, Object[] args, String methodName, String returnClass, String []jsignature);

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



