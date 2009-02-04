
package org.omegahat.Interfaces.NativeInterface;

import org.omegahat.Environment.Interpreter.Evaluator;

/**
  This class is used to implement a lookup table
  to determine whether a Java object can be converted
  directly to an R object or whether a reference must be used.
  This is user, run-time extensible in that one can add a class
  to the list of known convertable classes.
  */
public class ConvertibleClassifier
        extends java.util.Hashtable
        implements ConvertibleClassifierInt
{
protected Evaluator evaluator;

public ConvertibleClassifier() {
     this(5);
}    

/**
  The standard constructor which receives the evaluator to use for resolving
  classes when a class is registered
 */
public ConvertibleClassifier(org.omegahat.Environment.Interpreter.Evaluator evaluator) {
     this(5);
     setEvaluator(evaluator);
}    

/**
  Allocates an initial capacity of <code>n</code>
*/
public ConvertibleClassifier(int n) {    
     super(n);
}    

/**
   This is really an internal method
 */
protected boolean add(Object o) {
 if(o instanceof String) {
  try {
   Class c =  getEvaluator().findClass((String)o);
    if(c != null)
      return(add(c));
  }  catch(ClassNotFoundException e) {

  }
 } else if(o instanceof Class) {
   super.put(o, new Integer(EXACT_MATCH));
   return(true);
 }


 throw new RuntimeException("Can only add classes or strings to a ConverterFactory");
}


/** 
  Main entry point for determining if the object can be converted.
  @see isConvertible(Object, Class)
 */
public boolean isConvertible(Object o) {
 if(o == null)
  return(true);

 return(isConvertible(o, o.getClass()));
}

/**
 Determines whether the object is considered convertible by 
 comparing it and/or its class with the entries in this
 classifier's table of classes that are know to be convertible.
 Additionally, this also handles the cases of the builtin primitives.
 The worst case scenario is that there is not an exact match of the class
 and we must search through all the elements in the table using their type
 of matching attribute registered with the class.
 */
protected boolean isConvertible(Object value, Class c)
{
  boolean ans = false;

  if(c.isArray()) {

   if(true)
     return(true);

   c = c.getComponentType();
      /* Avoid arrays of arrays of arrays
         We check the element type of an array class
         to see if it is also an array.
       */
   if(c.isArray())  {
     return(!c.getComponentType().isArray());
   }
   ans = isConvertible(value, c);
  } else {
    if(c == Byte.class)
      return(false);
    ans = org.omegahat.Environment.Interpreter.BasicEvaluator.isPrimitive(c)
               || c.equals(String.class);
    if(!ans) {
      ans = containsKey(c);
      if(!ans) {
          /* Long check */
        ans = longCheck(c, value);
      }
    }
  }

 return(ans);
}

/**
  This performs the lengthy check comparing the actual object
  being converted (<code>value</code>) against the different
  classes registered with this classifier to determine if 
  it is compatible with any of these classes.
  This is where the type of specified when the class was registered is used.
 */
protected boolean longCheck(Class c, Object value) 
{
 int type;
 Class k;
 boolean ans;

  for(java.util.Enumeration e = keys(); e.hasMoreElements() ; ) {
      k = (Class)e.nextElement();
      type= ((Integer)get(k)).intValue();
      switch(type) {
            case ASSIGNABLE_FROM_MATCH:
             ans = k.isAssignableFrom(c);
             break;
            case INSTANCEOF_MATCH:
             ans = k.isInstance(value);
             break;
            default:
               continue;
      }
      if(ans) {
System.err.println("Match for " + k + " for " + value.getClass());
        return(true);
      }
  }

 return(false);
}



public void setConvertible(Class c, boolean ok) {
 setConvertible(c, EXACT_MATCH, ok);
}

public void setConvertible(Class c, int type, boolean ok) {
 if(ok == false)
   remove(c);
 else {
   put(c, new Integer(type));
 }
}

/**
   Accessor for {@link #evaluator evaluator} field
 */
public Evaluator getEvaluator()
{
   return(evaluator);
}

/**
   Accessor for setting {@link #evaluator evaluator} field
 */

public Evaluator setEvaluator(Evaluator value)
{
   evaluator = value;
  return(evaluator);
}


    
public String[] getConvertibleClasses() {

  int n = size(), i = 0;
  String[] ans = new String[n];
  for(java.util.Enumeration e = keys(); e.hasMoreElements() ; i++) {
    ans[i] = ((Class)e.nextElement()).getName();
  }

 return(ans);
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

