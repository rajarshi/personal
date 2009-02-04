
package org.omegahat.Interfaces.NativeInterface;



/**
   Interface defining the concept of an R or S object that is being
   exported to Java via the embedded JVM mechanism, and the methods
   that this should support.
   These methods contain information for identifying the object (i.e. the name and the database)
   in that remote system (R or S) when the JVM makes the call back to it;
   identifying the Java class to use for representing this object in the local (Java) environment;
   the names of one more interfaces or classes to implement when dynamically generating a new proxy
   class to represent this object locally.
  */
public interface ForeignReferenceInt  {
public String getReferenceName();

/**
   Callback to the foreign system with all the details of the Java method being called.
   This is all the caller need supply. When we pass control over to the foreign system,
   we may want to add the class identity of the caller and perhaps an anonymous
   reference to the `<code>this</code>' whose Java method is being called.
   This is not entirely necessary as the foreign system can take care of that,
   but it may be useful.
  */
public Object eval(org.omegahat.Environment.Parser.Parse.List args,
                    String methodName, String returnClass, String []signature)
    throws Exception;


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

