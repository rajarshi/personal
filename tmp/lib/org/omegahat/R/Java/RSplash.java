
package org.omegahat.R.Java;



/**
   A splash screen to announce that R is being started
   within the Java process. This has the beneficial side
   effect of forcing the GUI libraries to be loaded in
   Java before the non-threaded libX11 is loaded by R.
   This is typically used in 
    {@link org.omegahat.R.Java.ROmegahatInterpreter ROmegahatInterpreter}
   when running R and one has the System property <code>"GUI"</code>
   set to any value.
  */
public class RSplash
      extends org.omegahat.Environment.GUITools.GenericFrame
{
public RSplash() {
 init();
}

public void init() {
 javax.swing.JLabel label = new javax.swing.JLabel("Starting R", javax.swing.SwingConstants.CENTER);
 component(label);
 setBounds(new java.awt.Rectangle(300,300, 100,50));
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

