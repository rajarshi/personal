
package org.omegahat.Interfaces.NativeInterface;

import jas.*;


public class ForeignReferenceClassGenerator
         extends org.omegahat.Environment.GUITools.EvaluableInterfaceGenerator
{

public ForeignReferenceClassGenerator()
{

}

public ForeignReferenceClassGenerator(String []baseTypes, String targetType)
         throws ClassNotFoundException
{
 super(baseTypes, targetType);
}

public ForeignReferenceClassGenerator(String baseType, String targetType)
         throws ClassNotFoundException
{
 this(baseType, targetType, true);
}

public ForeignReferenceClassGenerator(String baseType, String targetType, boolean make)
         throws ClassNotFoundException
{
 super(baseType, targetType, false);
 superClassName(System.getProperty("ForeignReferenceBaseClass", "org/omegahat/R/Java/RForeignReference"));
 if(make)
  make();
}


/**
   Overrides the method to add space for the return type
   and the signature and elements.
  */

protected int numLocalVariables(int numArgs)
{
 return(10);
}

int
createSignatureArray(java.lang.reflect.Method m, CodeAttr code)
    throws jas.jasError 
{
 Class[] params = m.getParameterTypes();
 int numParams = (params != null ? params.length : 0);
 int signatureArrayVarNum = numLocalVariables(numParams)-1; // Need to compute this.


   /* Create the array. */

  code.addInsn(new Insn(opc_bipush, numParams));
  code.addInsn(new Insn(opc_anewarray, new ClassCP("java/lang/String")));
  code.addInsn(new Insn(opc_astore, signatureArrayVarNum));


 StringCP type;
 for(int i = 0; i < numParams ; i++) {
    type = new StringCP(params[i].getName());
    classDef().addCPItem(type);

    /* Now add each entry into the array.
       pop the array, the index and then the value onto the stack.
     */

    code.addInsn(new Insn(opc_aload, signatureArrayVarNum));
    code.addInsn(new Insn(opc_bipush, i));
    code.addInsn(new Insn(opc_ldc, type));
    code.addInsn(new Insn(opc_aastore));
 }

 return(signatureArrayVarNum);
}


public int addMethod(java.lang.reflect.Method m) {
  CodeAttr code = new CodeAttr();
  StringBuffer signature = new StringBuffer(200);

  int argListVar = createArgumentList(code, m, signature);
  String returnClass = className(m.getReturnType(), true);
  int signatureArrayVar;

  try {
   StringCP cp;

      signatureArrayVar = createSignatureArray(m, code);

         // push the `this' (aload 0) onto the stack.
       code.addInsn(new Insn(opc_aload,0));
           /* Push argument list onto stack. */
       code.addInsn(new Insn(opc_aload, argListVar));

       cp = new StringCP(m.getName());
       classDef().addCPItem(cp);
       code.addInsn(new Insn(opc_ldc, cp));

       cp = new StringCP(m.getReturnType().getName());
       classDef().addCPItem(cp);
       code.addInsn(new Insn(opc_ldc, cp));

       code.addInsn(new Insn(opc_aload, signatureArrayVar));
//       code.addInsn(new Insn(opc_checkcast, new ClassCP("[Ljava/lang/String;")));
          /* Call 
               eval(List) with  the FunctionCallArguments as the argument
             and
           */
        addDispatchCall(code, m.getName(), signature.toString(), returnClass);
 
    addReturn(m.getReturnType(), code);
  } catch(jasError err) {
System.err.println("Error generating code for " + m.getName());
     err.printStackTrace();
  }



    // now fixup the signature with () and the return type.
 String completeSignature = "(" + signature.toString() + ")" + returnClass;

if(Debug())
 System.out.println("Method " + m.getName() + " " + completeSignature);

  classDef().addMethod((short)ACC_PUBLIC, m.getName(), 
                        completeSignature,
                        code, null); // no exceptions.

 return(argListVar);
}

/**
 Method that allows derived classes to change the way the generated methods actually
 invoke other methods to get things done, having gathered the arguments into an 
 {@link org.omegahat.Envronment.Parser.Parse.ArgList ArgList}
*/
protected boolean addDispatchCall(CodeAttr code, String methodName, String signature, String returnClass)
              throws jas.jasError
{
       code.addInsn(new Insn(opc_invokenonvirtual,
                               new MethodCP(superClassName(),
                                     "localEval", 
                                     "(Lorg/omegahat/Environment/Parser/Parse/List;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)Ljava/lang/Object;"
                                     )
                        )
               );

 return(true);
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

