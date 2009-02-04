

import org.omegahat.R.Java.ROmegahatInterpreter;
import org.omegahat.R.Java.REvaluator;


public class JavaRCall
{
static public void main(String[] args) {
  ROmegahatInterpreter interp = new ROmegahatInterpreter(ROmegahatInterpreter.fixArgs(args), false);
  REvaluator e = new REvaluator();

  Object[] funArgs;
  String[] objects;
  Object value;
  int i;

System.err.println("[search]");
  String[] search =(String[]) e.eval("search()");
  if(search != null) {
    interp.show(search);
  }

for(i = 0; i < 4; i++) {
  search =(String[]) e.eval("search()");
  if(search != null) {
    interp.show(search);
  }
}

 
System.err.println("[objects]");
  objects =(String[]) e.eval("objects('package:base')");
  if(objects != null) {
    interp.show(objects);
  }

System.err.println("function call [objects('package:base')]");
  funArgs = new Object[1];
   funArgs[0] = "package:base";
  objects = (String[]) e.call("objects", funArgs);
  if(objects != null) {
    interp.show(objects);
  }

  System.err.println("Simple normal rnorm(10)");
   funArgs = new Object[1];
   funArgs[0]  = new Integer(10);
    value =  e.call("rnorm", funArgs);
    if(value != null) {
      interp.show(value);
    }

  System.err.println("Normal rnorm(7, 100, 11)");
   funArgs = new Object[3];
   funArgs[0] = new Integer(7);
   funArgs[1] = new Double(100);
   funArgs[2] = new Double(11);
   System.err.println("Calling rnorm()");
    value =  e.call("rnorm", funArgs); //, new String[3]); //(String[])null);
    if(value != null) {
     interp.show(value); 
      value =  e.call("mean", new Object[]{value});
      interp.show(value);
  }

    


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

