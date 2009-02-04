
package org.omegahat.R.Java;



/**
   An Omegahat interpreter that also loads and initializes an embedded
   R interpreter and that can optionally pass control to it to get the
   usual R prompt.
   This is used to bring R into a Java application.
   One typically invokes this once simply to load and initialize the R session
   and then create an instance of the {@link org.omegahat.R.Java.REvaluator REvaluator}
   class to call S functions and evaluate S expressions.
   This class provides a {@link #run() run()} method which causes this
   Java thread to enter the R internal event loop, effectively running
   R but from within a Java application.  <font color="red"><i>This is especially prudent
   given the difficulties of matching and loading thread-safe libraries
   when running Java within R.</i></font>
 */
public class ROmegahatInterpreter
   extends org.omegahat.Interfaces.NativeInterface.OmegaInterfaceManager
   implements Runnable
{
  /** 
      Basic command line arguments used in the case the caller doesn't provide
      any when initializing R via this class.
   */
 static String DefaultCmdArgs[] = {"JavaR"};


 static {
   try {
    System.err.println("Loading RInterpreter library");
    System.loadLibrary("RInterpreter");
   } catch(Exception ex) {
      System.err.println(".... Failed to load RInterpreter library.");
      System.err.println("Check your LD_LIBRARY_PATH, etc. and whether R is compiled as a shared library.");
      ex.printStackTrace();
   }
 }

/**
 Basic constructor for this class which initializes
 R with the specified command line arguments
 and runs the R event loop.
*/
public ROmegahatInterpreter(String[] args)
{
 this(args, true);
}

/**
 Constructor that initializes R and optionally runs its event loop.
 A GUI splash screen will be displayed if the system property GUI
 is non-null.
*/
public ROmegahatInterpreter(String[] args, boolean run)
{
  RSplash splash = null;
  if(System.getProperty("GUI") != null) {
    splash = new RSplash();
    splash.setVisible(true);
  }
  
  initR(args);
  nativeSetup(this, null);
  registerNativeConverters();

  if(splash != null) {
    splash.setVisible(false);
    splash.dispose();
    splash = null;
  }
  if(run) {
    run();
  }
}  


/**
 The entry point for using this as a top-level, stand-alone application
 which simply runs R within Java, but getting the low-level C libraries
 loaded (more) correctly.
*/
static public void main(String[] args) {
  args = fixArgs(args);

  ROmegahatInterpreter interp = new ROmegahatInterpreter(args, true);
}

/**
 Native method that initializes the R engine and interpreter
 making it available for evaluating expressions.
*/
public native boolean initR(String[] args);

/**
 User-level (i.e. Java) control for causing the 
 default converters controlling how S objects are converted
 to Java and vice-versa to be registered and used  when 
 passing data across the S-Java interface.
*/
public native boolean registerNativeConverters();

/**
 A native method that simply runs the R event loop,
 currently never returning from this call.
*/
public native void run();

/**
 This takes the user's command line arguments and 
 adds the equivalent of <code>argv[0]</code> (the name of the
 application being invoked)
 to them so that they can be passed to and correctly understood by the
 R engine. 
*/
static public String[] fixArgs(String []args)
{
  if(args!=null && args.length > 0) {
      /* Massage the arguments so that we add argv[0] to the front. */
    String []tmp = new String[args.length +1];
     tmp[0] = DefaultCmdArgs[0];
        /* Copy the user's arguments. */
     for(int i = 0; i < args.length; i++)
        tmp[i+1] = args[i];
     args = tmp;
  } else
     args = DefaultCmdArgs;

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
