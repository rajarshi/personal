
package org.omegahat.R.Java;


/**
 A class that allows an R object (function or closure) to be used
 as an ActionListener for Swing buttons, etc. so that when an event
 happens on that component, the appropriate R function is invoked.
 This differs from the automatically generated versions (using
 the dynamic compiler via {@link org.omegahat.Interfaces.NativeInterface.ForeignReferenceClassGenerator
 ForeignReferenceClassGenerator}) in that 
<ul>
<li> the Java method called by the  JButton is native
  (rather than a method that arranges its arguments into a List
   and calls an inherited native method with these arguments,
   the name of the method, and the signature)
<li> the foreing R object that is to be invoked by the 
  native <code>actionPerformed</code> method should be
  a function, not a list of functions.
</ul>
*/
public class RManualFunctionActionListener
          extends RForeignReference
          implements java.awt.event.ActionListener
{
static {
 try {
  System.loadLibrary("SJava");
 } catch(Throwable t) {
  System.err.println("Can't load SJava DLL/shared library: " + t.getMessage());
  t.printStackTrace();
 }
};

public RManualFunctionActionListener(String id) {
 super(id);
 System.err.println("Created R function listener "  + getReferenceName());
}

public RManualFunctionActionListener(String id, org.omegahat.Interfaces.NativeInterface.OmegaInterfaceManager evaluator) {
 super(id, evaluator);
}

public RManualFunctionActionListener(org.omegahat.Interfaces.NativeInterface.ForeignReference ref, org.omegahat.Interfaces.NativeInterface.OmegaInterfaceManager evaluator) {
 super(ref, evaluator);
}

public RManualFunctionActionListener(org.omegahat.Interfaces.NativeInterface.ForeignReference ref) {
 super(ref);
}
    
public native void actionPerformed(java.awt.event.ActionEvent ev);

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

