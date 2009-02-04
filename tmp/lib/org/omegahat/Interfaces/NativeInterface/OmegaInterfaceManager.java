package org.omegahat.Interfaces.NativeInterface;


import org.omegahat.Environment.Interpreter.Evaluator;
import org.omegahat.Environment.Interpreter.Options;

import org.omegahat.Environment.Parser.Parse.ExpressionInt;
import org.omegahat.Environment.Parser.Parse.List;
import org.omegahat.Environment.Parser.Parse.ArgList;
import org.omegahat.Environment.Parser.Parse.Name;
import org.omegahat.Environment.Parser.Parse.ConstantExpression;
import org.omegahat.Environment.Parser.Parse.DynamicFieldAccess;
import org.omegahat.Environment.Parser.Parse.AssignExpression;
import org.omegahat.Environment.Parser.Parse.ConstructorExpression;
import org.omegahat.Environment.Parser.Parse.MethodCall;

import org.omegahat.Environment.Databases.Database;
import org.omegahat.Environment.Databases.EvaluationFrame;


import java.lang.reflect.Array;



/**
 An extension of the interactive evaluator
 which can be used to embedded Omegahat within foreign systems such as S or R
 for brokering object creation and method invocation.
 This handles exporting named and anonymous references
 to the foreign system,  and understands which types
 of objects can be converted from one system to the other.
 It also provides several additional convenience methods
 that simplify actions in the foreign system.
*/
public class OmegaInterfaceManager 
               extends org.omegahat.Environment.Interpreter.InteractiveEvaluator         
{
static public boolean PublicFlag = true;


private static Class[] wrapperClasses ={ java.lang.Boolean.class, java.lang.Byte.class, 
                                         java.lang.Character.class, java.lang.Short.class,
                                         java.lang.Integer.class, java.lang.Long.class,
                                         java.lang.Float.class, java.lang.Double.class,
                                         java.lang.String.class, java.lang.Object.class
                                       };

private static String[] arrayNames = {"[Z", "[B", "[C", "[S", "[I", "[J", "[F", "[D",
                                      "[java.lang.String;", "[java.lang.Object;"};
private static final int booleanArray = 0;
private static final int byteArray = 1;
private static final int characterArray = 2;
private static final int shortArray = 3;
private static final int integerArray = 4;
private static final int floatArray = 5;
private static final int doubleArray = 6;
private static final int stringArray = 7;
private static final int objectArray = 8;
private static Class[] primitiveClasses;
private static Class[] arrayClasses;


    /* initialize the Class arrays */
static {
  int n = wrapperClasses.length;
  primitiveClasses = new Class[n];
  arrayClasses = new Class[n];

  for(int i=0; i< n; i++) {
      try {

        primitiveClasses[i] = (Class)wrapperClasses[i].getField("TYPE").get(null);
          // this won't happen if there is an error because the field doesn't exist.
          // i.e. String.
        arrayClasses[i] = Class.forName(arrayNames[i]);

      } catch(Exception e) {  // NoSuchFieldException or illegal access.
           /* that's ok, assume it wasn't really primitive */
        primitiveClasses[i] = wrapperClasses[i];
        arrayClasses[i] = Array.newInstance(wrapperClasses[i], 0).getClass();
      }
  }
}




static public Class[] getWrapperClasses()
{
  return wrapperClasses;
}

static public Class[] getPrimitiveClasses()
{
  return primitiveClasses;
}

static public Class[] getArrayClasses()
{
  return arrayClasses;
}

public Class[][] getAllClasses()
{
  Class[][] value = new Class[3][];
  value[0] = wrapperClasses;
  value[1] = primitiveClasses;
  value[2] = arrayClasses;
  return value;
}


  /** The database used to store
      the objects which are not explicitly named
      in a call from another system across JNI, and
      each is thus given an arbitrary name which is returned
      to the caller.
   */
protected Database anonymousDatabase;


/**
 The default database in which objects that are exported
 as named references are stored. This is attach as the
 value of {@link defaultDatabase defaultDatabase()}.
*/
protected Database db;

 /** Flag controlling whether debugging information is displayed. */
protected int debug = 0;



protected long count = 0;

protected ConvertibleClassifierInt converterFactory;

/**
 Default constructor that creates its own databases
 for storing the named and anonymous references
 and registers itself as the default evaluator, 
 creates basic variables for referencing the
 evaluator and manager (one and the same) from R/S.
 This also sets the output stream of the evaluator
 to null, just like disablin standard output since this
 is being run as an embedded Omegahat/Java evaluator inside R.
*/
public OmegaInterfaceManager()  {
 this(true);
}

/**
 Default constructor that creates its own databases
 for storing the named and anonymous references
 and registers itself as the default evaluator
*/
public OmegaInterfaceManager(boolean register)  {
  super();
  database(new InterfaceDatabase());
  evaluator().defaultDatabase(database());

if(register) {
    try {
     org.omegahat.Environment.Interpreter.EvaluatorManager mgr =
              new org.omegahat.Environment.Interpreter.EvaluatorManager(this);
     org.omegahat.Environment.System.Globals.manager(mgr);     
     org.omegahat.Environment.System.Globals.evaluator(this);      
    } catch(ClassNotFoundException ex) {
  
    }
}

try {
 if(debug()) {    
  System.err.println("# elements in search path " + searchPath().size());
  System.err.println("properties: " + System.getProperties());
  System.err.println("Attempting to find class in " + classLists().size());

  for(java.util.Enumeration e = classLists().elements(); e.hasMoreElements(); ) {
    System.err.println(((org.omegahat.Environment.Tools.ClassList.ObjectList) e.nextElement()).file());
   }

 System.err.println("class path " + System.getProperty("java.class.path"));    
 }


/*
System.err.println("Attempting to find class:");
Class c = findClass("Vector");
System.err.println("\t"+c);
*/
 assign("__Evaluator", this);
 assign("__Manager", this);

/*
   Temporary. Gives us something to test.
   The .JavaConstructor is working however.
*/

 java.util.Vector v = new java.util.Vector(3);
  v.addElement(new Integer(101));
  v.addElement("Test string");
 assign("x", v);
 String[] a = new String[3];
    a[0] = "Omegahat";
    a[1] = "R1.1.0";
    a[2] = "S4";
 assign("xx", a);

} catch(Exception ex) {
  System.err.println("Didn't setup initial values");
}

 /* Display the initial databases. */
if(debug()) {
 System.err.println((database()==defaultDatabase()));
 System.err.println(defaultDatabase());
 System.err.println(database());
}

 output(null);

 setConvertibleClassifier();
}


  /**
     Create the manager, reading the 
       {@link Options Options} name-value pairs from the 
       specified stream.
    @param stream input stream containing the  name-value
      property values parameterizing the manager
      and its options.
   */

public OmegaInterfaceManager(java.io.InputStream stream)  {
 super(new Options(stream));
}

   /**
      Create the manager, reading the {@link Options Options}  
      from the specified file.
      @param f the file containing the name-value properties
         parameterizing the manager and its different
         options.
   */
public OmegaInterfaceManager(java.io.File f)  {
 super(new Options(f));
}


  /**
      Create the manager, reading the properties specifying
      the {@link Options Options} from the file
      identified by <code>name</code> or contained
      in the text of <code>name</code> if it does not 
      refer to a file.
    */
public OmegaInterfaceManager(String name) throws Throwable {
 super(new Options(name));
}



 /** Query whether debug information is displayed. */
public boolean debug() {
 return(debug(0));
}

/**
  Set the level at which debugging information
  should be displayed. One can conditionally
  execute code at different levels of debugging
such as 
<pre>
  if(debug(3)) {
    // execute some code
  }
</pre>
   which allows the user to skip over code by setting the
   debug level to exclude that level and higher.
 */
public boolean debug(int level) {
 return(debug > level);
}

 /** Specify whether debug information is displayed. */
public boolean debug(boolean d) { 
  debug = 1;
 return(debug());
}

/**
  Set the level of debugging so that code segments
 surrounded by a call to <code>debug(n)</code>  will only be executed
 if <code>level</code> is greater than <code>n</code>.
  @param level the value above which debugging output is not
   displayed
 */
public boolean setDebug(int level) { 
  debug = level;
 return(debug());
}


/**
 An internal method for evaluating an expression and assigning
 the value to the specified name in the named database.
 This is called by other methods that take the expression formulation
 from R/S as a string and convert it to an expression.
 @see evalConstructor(String,String[],String)
*/
protected String eval(String resultName, ExpressionInt expr) throws Throwable {

  evaluator().evaluate(new AssignExpression(new Name(resultName), expr));

 return(resultName);
}


  /**
       Creates a new object of the specified class,
       invoking the constructor with the given arguments
       having assigned any with a permanent name to the
       regular {@link #database() database} for future use.
       The usual "conversion" mechanism is employed to 
       provide the return value. See 
         {@link #assignResult(java.lang.String,java.lang.Object,boolean) assignResult}
<p>
       This is usually invoked by the calling interface from S or R.

      @param className the partially qualified name of a Java class
         sufficient to identify the appropriate class using the 
         class lookup mechanism of Omegahat.

      @param args an array of arguments which are passed to the
            appropriate constructor of the specified class.
      @param argumentKeys  either null or an array of names 
              parallel to the <code>args</code> array
             in which any non-trivial values cause that value
             to be "permanently" assigned as Omegahat variables
             in the default database associated with this manager.
      @param resultKey the name of the Omegahat variable to which the result should be 
              assigned, or <code>null</code> or <code>""</code> to cause it be
              anonyomized or converted.
                  
    */
public Object callConstructor(String className, Object[] args,
                                String[] argumentKeys, String resultKey, boolean convert)
 throws Throwable
{
if(debug()) {
  System.err.println("Argument names " + argumentKeys);
  if(argumentKeys != null)
    System.err.println("\t length " + argumentKeys.length);
}

 assignArguments(args, argumentKeys);

 ConstructorExpression c = new ConstructorExpression(className, args, true);
 Object val = null;

 val = c.eval(evaluator());


/*
 Don't do this now. Leave the C-code comeback and make it a reference
 if _it_ determines the value can not be converted.

 val = assignResult(resultKey, val, convert);
*/ 

if(debug()) {
  System.err.println("Return name for constructor " + resultKey + " " + (val == null ? "" : val.getClass().getName()));
  System.err.println(val.getClass());
}

return(val);
}


public String evalConstructor(String className, String[] argumentKeys, String resultKey) 
 throws Throwable
{
 ExpressionInt expr = null;
   expr = new ConstructorExpression(className, argsToList(argumentKeys));
 return(eval(resultKey, expr));
}

public String evalConstructor(String className, Object[] args,
                                String[] argumentKeys, String resultKey)
{
//  return(val);
 return(resultKey);
} 


/**
 
*/
public String evalMethod(String objKey, String methodName, String[] argumentKeys, String resultKey) 
  throws Throwable
{
 ExpressionInt expr = null;
    expr = new MethodCall(new Name(objKey), methodName, argsToList(argumentKeys));
 if(resultKey != null) {
   expr = new AssignExpression(new Name(resultKey), expr);
 }

  return(eval(resultKey, expr));
}



  /** 
     Construct an object that will convert the names
     to objects by looking up the databases.
   */
public List argsToList(String[] names) {
 List l = null;
 if(names == null || names.length < 1)
   return(new List()); // currently necessary, but the expressions should handle null arguments.

 int n = names.length;
  l = new List(n);
 for(int i = 0; i < n; i++) 
   l.addElement(new Name(names[i]));

return(l);
}



 /**
     Assign the value to the specified name as an Omegahat variable
     in the default database of the associated 
      {@link org.omegahat.Environment.Interpreter.Evaluator Evaluator}.
   */
/*
public boolean assign(String name, Object  val) throws Exception {
 assign(name, val);
 return(true);
}
*/


/**
  Assign the value to the anonymous database
   ({@link #anonymousDatabase() anonymousDatabase})
  using the next available counter as the key/variable name
  for the object.

  @return the name used as the variable in the database
     for storing this value. This is guaranteed to be unique
     (up to a very large number of objects being added!)
 */
public Object anonymousAssign(Object val) throws Exception {
   count++;
   String s = "";
    s+= count;

 if(anonymousDatabase == null)
   anonymousDatabase = new InterfaceDatabase("Anonymous " + defaultDatabase().getName());

  anonymousDatabase.assign(s, val);

  Object o = new AnonymousReference(s, this, anonymousDatabase, val.getClass().getName());

return(o);
}



/**
   Remove the variable from one of the evaluator manager's database.
   This calls the standard <code>remove</code> method 
   from {@link org.omegahat.Environment.Interpreter.Evaluator Evaluator}
   and if this fails (because there is no such variable there),
   then we look in the anonymous database and attempt to do the same.

   @see clearReference(String,boolean)
 */
public boolean remove(String name) 
{
 boolean ans = super.remove(name);
 if(ans == false) {
   ans = anonymousDatabase().exists(name);
   if(ans)
    anonymousDatabase().remove(name);
 }

 return(ans);
}

/**
  Generic remove method that can handle an argument which
  is a String or  a Named or Anonymous reference.
 */
public boolean remove(Object o) throws Exception 
{
 boolean ans = false;

 if(o instanceof String)
  ans = remove((String)o);
 else if(o instanceof NamedReference) {
   ans = remove((NamedReference)o);
 } else if(o instanceof AnonymousReference) {
   ans = remove((AnonymousReference)o);
 } else 
    throw new Exception("Incorrect type");

 return(ans);
}

/**
 Remove the named reference from the named database.
 */
public boolean remove(NamedReference r) {
  return(super.remove(r.key()));
}

/**
 Remove the specified reference from the anonymous database.
 */
public boolean remove(AnonymousReference r) {
 return( remove(r.key(), anonymousDatabase()) ); 
}

/**
 Remove the specified variable from the given database
 @return true if the database has a variable with the given name
  (and it was removed).
 */
public boolean remove(String key, Database db) {
 boolean ans = db.exists(key);
   if(ans)
    db.remove(key);

 return(ans);
}


/**
 Motivated by need to resolve a reference in native C code from
 R when creating an R-Java graphics device.
*/
public Object getReferenceValue(Object obj)
    throws Throwable
{
 Object val = null;
  if(obj instanceof InterfaceReference) {
    val = ((InterfaceReference)obj).eval(this);
    return(val);
  } else if(obj instanceof String) {
     if(exists((String) obj)) {
        val = get((String) obj);
     } else {
       if(anonymousDatabase().exists((String) obj))
         val = anonymousDatabase().get((String) obj);
     }

    if(val != null && val instanceof InterfaceReference)
      val = ((InterfaceReference) val).eval(this);

System.err.println("[getReferenceValue] " + obj + " ->> " + val);
  }

 return(val);
}

public Object getReferenceValue(String name, boolean anonymous)
{
 org.omegahat.Environment.Databases.Database db = anonymous ? anonymousDatabase() : defaultDatabase();

  return(db.get(name));

}


/**
   Work horse method for the interface manager. Handles
   field access and method calls for instance and static
   calls.
  */
public Object genericCallMethod(String []qualifier, Object[] args, String[] names,
                                  String returnName, boolean convert)
                   throws Throwable
{
  MethodCall call = new MethodCall(new Name(qualifier), new List(args));

  return(genericCallMethod(call, returnName, convert));
}


public Object genericCallMethod(String qualifier, String methodName, 
                                 Object[] args, String[] names,
                                   String returnName, boolean convert)
                 throws Throwable  
{
  MethodCall call;
  
  if(debug())
    System.err.println("[A] # arguments "+ (args == null ? 0 : args.length));

  if(qualifier != null) 
      call = new MethodCall(new Name(qualifier),
                                    methodName, new ArgList(args));
  else
      call = new MethodCall(new Name(methodName), new ArgList(args));


  if(debug())  
    System.err.println("[A] # call arguments "+ call.args().size());

  return(genericCallMethod(call, returnName, convert));
}

/**
  Invoke the method identified by <code>methodName</code>
  on the specified object - be it a expression or
  regular object - passing it the <code>args</code>
  as arguments. 
  This is called from the native C routines that form the
  bridge between R/S and Omegahat. 
  The <code>names</code> argument specifies which of the arguments
  are to be stored in the regular named database for use in future
  calls from the foreign system.

  @param qualifier an object from which we derive the actual object
   whose method is to be invoked. This object becomes the
   <code>this</code> in the method call. The argument can be an actual object
   or an expression which is evaluated to produce the target object.

  @param the name of the method in the qualfier object's class
   that is to be invoked. The types of the arguments allow Omegahat
   to determine the appropriate method with this name even with 
   method overloading.

  @param args the collection of arguments to be passed to the 
   method call that is being invoked here.

  @param returnName if this is non-null, the return value from the method call
  is stored in the named database and no attempt to convert to the foreign
  system is made. A reference to the object is passed to that foreign system.
  This naming mechanism allows one to store an intermediate object that is 
  to be used in future calls from the foreign system.

  @param convert whether to attempt to convert the return value of
  the method call when passing it back to the foreign system.
  If this is <code>false</code>, we just assign it to the 
  anonymous database and return a reference to it. Otherwise,
  Omegahat determines whether the object is convertible and if so
  passes it to the C routines that perform the conversion to an 
  object in the foreign system.
*/
public Object genericCallMethod(Object qualifier, 
                                 String methodName, 
                                  Object[] args, String[] names,
                                    String returnName, boolean convert)
           throws Throwable
{
 Object ans = null;

  if(debug()) {
   if(args != null && args.length  > 0) {
    for(int i = 0 ; i < args.length; i++) {
      System.err.println(i+") " + args[i].getClass());
    }
   } else {
      System.err.println("no arguments");
   }
   
   System.err.println("Argument names " + names);
   if(names != null)
     System.err.println("\t" + names.length);

   if(debug()) {   
     if(qualifier == null)
       System.err.println(" using evaluator as qualifier");
     else
       System.err.println(" [genericCallMethod] qualifier " + qualifier.getClass());
   }
  } /* end of if(debug()) */ 

  /* If the user specified name for any of the arguments,
     assign the values to the database for future reference by the caller.
   */ 
 if(names != null && names.length > 0)
   assignArguments(args, names);

  /*
     Now we do the method dispatching to the local handler based on the type
     of object we got as the qualifier in the real method call.
   */
 if(qualifier == null || qualifier instanceof String) {
   ans = genericCallMethod((String) qualifier,
                            methodName, args, names, returnName, convert);
 } else if(qualifier instanceof  org.omegahat.Environment.Language.Evaluable){  // InterfaceReference) {
   ans = genericCallMethod((org.omegahat.Environment.Language.Evaluable)qualifier,
                            methodName, args, names, returnName, convert);
 } else if(qualifier.getClass().isArray() &&
              qualifier.getClass().getComponentType().equals(String.class)) {
   
   ans = genericCallMethod((String[]) qualifier, args, names, returnName, convert);
 }


 if(debug(1)) {
   System.err.println("Answer is " + ans + " " + (ans != null ? ans.getClass().getName() : "null"));
  }

  return(ans);
}



/**
  This version evaluates the target object on which the method
   or field access is being performed and then passes this
   to the the regular evaluation.
 */

public Object genericCallMethod(org.omegahat.Environment.Language.Evaluable qualifier, //InterfaceReference qualifier, 
                                 String methodName, 
                                  Object[] args, String[] names,
                                    String returnName, boolean convert)
     throws Throwable
{
 Object ans = null;
 Object This;
   /* 
      Resolve the reference. An exception should be thrown
      if the reference does not identify an element
      in the appropriate database(s). 
      As a result, if we get back a value, it is valid. It
      may be null, but that is the value stored in the reference.
    */
 This = qualifier.eval(evaluator());

 
 MethodCall call =   new MethodCall(new ConstantExpression(This), 
                                     methodName, new ArgList(args));

  return(genericCallMethod(call, returnName, convert));
}



public Object genericCallMethod(MethodCall call, 
                                    String returnName, boolean convert)
                 throws Throwable
{
  Object ans = null;
  try { 
    if(debug()) {
       System.err.println("[genericCallMethod(call, name)] " + call + " " + call.getClass());
       System.err.println("\t # arguments " + call.args().size());
     } 

     ans = call.eval(evaluator());
   } catch(NoSuchMethodException ex) {

       try {
         DynamicFieldAccess fieldCall =  new DynamicFieldAccess(call.qualifier(), call.methodName());
         ans = fieldCall.eval(evaluator());
       } catch (Exception e) {
         throw ex;
       }
   }


    if(debug() && ans != null) 
       System.err.println("Answer is " + ans);

/*
 Don't do this now. Leave the C-code comeback and make it a reference
 if _it_ determines the value can not be converted.

   ans = assignResult(returnName, ans, convert);
*/

    return(ans);
}




/**
    Iterates over the names (if there are any)
    and for non-empty strings, assigns the corresponding
    element in the object vector to that name.
    This is used to make arguments \OMega{} variables
    that are available in future calls.
  */
public int assignArguments(Object[] args, String [] argNames)
{
 int count = 0;
 if(argNames != null) {
  for(int i = 0; i < argNames.length; i++) {
    if(argNames[i] != null && argNames[i].equals("") == false) {
     try {
      if(debug())
        System.err.println("Assigning to " + argNames[i]);

      assign(argNames[i], args[i]);
      count++;
     } catch(Exception e) {
        e.printStackTrace();
     } 
    }
  }

  if(debug())
   System.err.println("Database: "+ database());
 }


 return(count);
}


 /**
    Evaluation of an Omegahat expression.
    Additional variables are provided in the @args array
    and are referenced in the expression by the names provided
    in the @argNames argument.
  */
public Object evaluate(String expression, Object[] args, String[] argNames, String returnName, boolean convert) {

if(debug())
 System.err.println("Evaluating expression "+expression);

 
 Database db = createCallFrame(args, argNames);

 org.omegahat.Environment.Interpreter.Evaluator evaluator;

    evaluator = this;


 Object obj = null;
 try {

if(debug())
 System.err.println("Starting the evaluation");

    /* If we have a call-frame, attach it. 
        This will change when the scoping rules become clearer.
     */
  if(db != null)
   evaluator.attach(db,0);

      /* Now, evaluate the expression. */
   obj = evaluator.evaluate(expression);
    /* Get the last element of the statement list result. */
   if(obj instanceof List) {
    obj = ((List)obj).elementAt(((List)obj).size()-1);
   }
 } catch(Throwable e) {
   e.printStackTrace();
 } finally {
    /* No matter what, detach the call frame,  if we attached.*/
   if(db != null)
    evaluator.detach(db);
 }

 /* Now convert the result.
    Actually, don't do this now. Leave the C-code comeback and make it a reference
    if _it_ determines the value can not be converted.

    obj = assignResult(returnName, obj, convert);
 */

return(obj);
}


/**
    Used when evaluating an \OMega{} expression in the
    manager with arguments specified in a frame
    like a substitute() function call in S.
    The arguments are reference by name in the expression,
    converted from user-level objects to Java objects and stored
    in this frame.
  */
public Database createCallFrame(Object[] args, String[] argNames)
{
 if((args == null || args.length == 0) || (argNames == null || argNames.length == 0))
   return(null);

 Database db = new EvaluationFrame("<OmegaInterfaceManager evaluation frame>");
 int n = Math.min(args.length, argNames.length);

 try {
   for(int i = 0; i < n ; i++) {
    db.assign(argNames[i], args[i]);
   }
 } catch(Exception ex) {
    ex.printStackTrace();
 }

 return(db);
}


/**
  Get a reference to the named reference database.
 */
public Database database() {
 return(db);
}

/**
    Set the default database for storing named references.
 */
public Database database(Database d) {
  db = d;
 return(database());
}



/**
  Retrieve a reference to the database in which the
  anonymous references are stored.
 */
public Database anonymousDatabase() {
 return(anonymousDatabase);
}

/**
  Set/replace the database which stores the
  anonymous references.
 */
public Database anonymousDatabase(Database db) {
  anonymousDatabase = db;
 return(anonymousDatabase());
}






/**
   Accessor for {@link #converterFactory converterFactory} field
 */
public ConvertibleClassifierInt getConvertibleClassifier()
{
   return(converterFactory);
}

/**
   Accessor for {@link #converterFactory converterFactory} field
 */
public void setConvertibleClassifier(ConvertibleClassifierInt f)
{
//System.err.println("Setting convertible classifier " + f + " " + f.getClass());
   converterFactory = f;
}

public void setConvertibleClassifier()
{
 String className = System.getProperty("ForeignConvertibleClassifierClass", 
                                       "org.omegahat.Interfaces.NativeInterface.BasicConvertibleClassifier");
 Object [] args = new Object[1];
  args[0] = this;
 try {
   Object o = new org.omegahat.Environment.Parser.Parse.ConstructorExpression(className, args).eval(this);
   if(o instanceof ConvertibleClassifierInt) {
      setConvertibleClassifier((ConvertibleClassifier)o);
   }
 } catch(Throwable t) {
   System.err.println("Cannot create foreign converter factory " + className +"\n\t" + t);
   System.err.println("Using BasicConvertibleClassifier");
   setConvertibleClassifier(new BasicConvertibleClassifier(this));   
 }
}




   /**
         Determines whether the specified object 
         can be converted to an object in the caller's
         own system based on the class of the object.

        @return logical indicating whether the object is convertible or not.
        @see #convertTable(java.lang.Class)
     */
public boolean convertable(Object value)
{
 boolean ans = false; // was false. Sep 1 2004

  if(value == null)
    return(ans);

  Class c = value.getClass();

  ans = getConvertibleClassifier().isConvertible(value);

 return(ans);


/**
 if(value == null)
  return(true);

 if(value instanceof java.util.Properties)
  return(true);

 return(convertable(value.getClass()));
*/
}

  /**
         Determines whether the specified object 
         can be converted to an object in the caller's
         own system, or whether it should be returned
         as a reference to the Java object.
<p>
     This is based on the set of primitives known to the
     interface at compile time.
     At present this knows about the S/R  and Java primitives.
     We can extend this to maintain a collection of additional
     Java and target-classes between which objects can be converted
     and the factory which can perform such conversions.
    
   */
public boolean convertable(Class c) {
 boolean ans = false;
 
 if(c == null)
   return(true);

 if(c.isArray()) {
   c = c.getComponentType();
   ans = convertable(c);
 } else {

  ans = ans || org.omegahat.Environment.Interpreter.BasicEvaluator.isPrimitive(c)
            || c.equals(String.class);

  ans = ans || InterfaceReference.class.isAssignableFrom(c);
 }

 if(debug())
    System.err.println(c.getName() + " convertable? " + ans);

 return(ans);
}


  /**
     High-level method that determines how to process the result
     of a Java call (constructor, method or field access, Omegahat expression).
     If the value <code>val</code> is convertible to a primitive type,
     then we simply return it. Otherwise, if the name to be used
     to store the value (<code>resultKey</code>) is non-trivial
     (i.e. is not null or ""), then we 
     assign the value  to the default database and create and return 
     a {@link NamedReference NamedReference} object.
     Otherwise, we create an {@link AnonymousReference AnonymousReference}
     object, having assigned the value to the  {@link #anonymousDatabase() anonymousDatabase}
     and return the reference.

     @param resultKey the name of the Omegahat variable to which the value 
          should be assigned in the regular database, or <code>null</code> or <code>""</code>
          indicating that we do not want it considered as an Omegahat variable.
     @param val the result of a Java call which is to be "returned"  to the
          caller across the interface.

     @return either the object itself, if it is a "primitive" that can be converted
        to an object in the callers language, or a specific instance of
        {@link InterfaceReference InterfaceReference}.

     @see #convertable().
    */
public Object assignResult(String resultKey, Object val)
           throws Exception
{
  return(assignResult(resultKey, val, true));
}

 /**
 If the caller allows us to convert (convert == true), 
 then check if we can convert this object. Otherwise,
  we claim it is not convertable and create a reference.
  If <code>resultKey</code> is non-null (or ""), then we create
  a named reference. Otherwise, an anonymous reference.
 @see #convertable(Object)
 */
public Object assignResult(String resultKey, Object val, boolean convert)
          throws Exception
{
 Object o = null;

 if(val == null)
    return(null);

   /* Don't create a reference to a reference. It may be desirable in some cases,
      but probably not.*/
 if(val instanceof org.omegahat.Interfaces.NativeInterface.InterfaceReference) {
   System.err.println("assigning an interface reference object. Leaving as is.");
   return(val);
 }
 
 boolean canConvert = convert;

 if(convert == true)
   canConvert = convertable(val);

 if((resultKey == null || resultKey.equals(""))) {
   if(canConvert) {
    return(val);
   } else {
       o = anonymousAssign(val);
   }
 } else {
       assign(resultKey, val);
       o = new NamedReference(resultKey, this, database(), val.getClass().getName());
 }

 return(o);
}


public String getDatabaseID() 
{
 return(database().getName());
}


/**
   Returns the class name of the specified object
   or null if the object is itself null.
   This is used in the C code communicating with the manager,
   although it may not be used anymore since we do not
   perform the recursive copies.
 */
public static String getDataType(Object o)
{
 if(o == null)
  return(null);

 return(o.getClass().getName());
}

/**
 This finds all the methods with the specified name
 in the class identified by the (partially qualified) 
 class name <code>className</code>.

 @param className the partially qualified name of a class
  which is resolved by this evaluator using
  {@link #findClass findClass()}.
 @param methodName the name of the method(s) of interest.
 @return either a  {@link java.lang.reflect.Method Method}
 object or an array of these objects.

 @see #getMethod(Class,String)
*/
public Object getMethod(String className, String methodName)
         throws ClassNotFoundException
{
 Class c = findClass(className);
 return(getMethod(c, methodName));
}


/**
 This finds all the methods with the specified name
 in the given class.

 @param c the class in which to look for the methods 
  with the name <code>methodName</code>
 @param methodName the name of the method(s) of interest.
 @return either a  {@link java.lang.reflect.Method Method}
 object or an array of these objects.

 @see #getMethod(Class,String)
*/
public Object
getMethod(Class c, String methodName)
{
 int i;
   java.lang.reflect.Method[] ms =  c.getMethods();
   java.util.Vector ans = new java.util.Vector(4);

   for(i = 0; i < ms.length; i++) {
     if(ms[i].getName().equals(methodName))
      ans.add(ms[i]);
   }

 if(ans.size() == 0)
    return(null);

 if(ans.size() == 1)
   return((java.lang.reflect.Method) ans.elementAt(0));

 java.lang.reflect.Method[] arr = new java.lang.reflect.Method[ans.size()];
 for(i = 0; i < arr.length; i++)
   arr[i] = (java.lang.reflect.Method) ans.elementAt(i);

 return(arr);
}

/**
Another form of the {@link #getMethods(Class,String) getMethods()}
method for use by the foreign system to discover
the methods it might call on an object.
*/
public java.lang.reflect.Method[] 
getMethods(String className)
      throws ClassNotFoundException
{
 return(getMethods(findClass(className)));
}

public java.lang.reflect.Method[] 
getMethods(Class c)
{
  return(c.getMethods());
}

/**
   Method to allow R calls to determine
   the constructors offered by a particular class
   which is identified by name.
   @see #getConstructors(Class)
 */
public java.lang.reflect.Constructor[]
getConstructors(String name)
  throws ClassNotFoundException
{
  return(getConstructors(findClass(name)));
}

/**
   Method to allow R calls to determine
   the constructors offered by a particular class.
 */
public java.lang.reflect.Constructor[]
getConstructors(Class c)
{
  return(c.getConstructors());
}



/**
  Method that can be easily called from R/S
  to retrieve a list of all the references in
  either the named or anonymous database.
  The internal default converters handle creating
  R objects from the Java references returned.
  @see getReferences(Database, boolean)
 */
public InterfaceReference[]
getReferences(boolean named)
{
 org.omegahat.Environment.Databases.Database db;
 if(named) {
   db = database();
 } else
   db = anonymousDatabase();

 InterfaceReference[] refs = getReferences(db, named);
 return(refs);
}

/**
  This is the method that computes the array of references
  for the elements within the specified database

 */
public InterfaceReference[]
getReferences(Database db, boolean named)
{
 if(db == null)
  return(null);


 String[] keys = db.objects();

 if(keys == null)
   return(null);

 int n = keys.length, i;

 InterfaceReference tmp;
 Object obj;
 String className;

 InterfaceReference []ans = new InterfaceReference[n];

  for(i = 0; i < n; i++) {
    obj = db.get(keys[i]);
    if(obj != null)
      className = obj.getClass().getName();
    else
      className = "";

    if(named)
      tmp =  new NamedReference(keys[i], this, db, className);
    else
      tmp = new AnonymousReference(keys[i], this, db, className);

    ans[i] = tmp;
  }

 return(ans);
}


/**
  Convenience method for clearing all the
 references from either the named or anonymous
 database. This can be called directly
 from R/S as
<pre>
    .Java(NULL, "clearReferences",T)
    .Java(NULL, "clearReferences",F)
</pre>
*/
public int clearReferences(boolean named) {
 Database db;
 if(named)
   db = database();
 else
   db = anonymousDatabase();

 int value = db.size();
 db.clear();

 return(value);
}


/**
 Removes the specified reference identified by name
 in either the named or anonymous database of references.
 This is  very similar to {@link #remove(String) remove(String)}.
 It differs in that it supports specification of which reference
 database the reference resides.
 It can be called directly from R/S as
<pre>
    .Java(NULL, "clearReferences", "b", T)
</pre>
*/
public boolean clearReference(String id, boolean named)
{
  return(remove(id, named ? database() : anonymousDatabase()));
}



/**
   Consumes the output of the task, hiding it from view.
 */
public boolean displayTask(org.omegahat.Environment.Interpreter.Task task) {

 return(true);
}



/**
  A convenience method that takes the partially qualified name
of a class and returns the fully qualfied name of the class
to which it resolves.  
  @param name the partially qualified name of the class of interest.
  @param forJNI  whether the resulting fully-qualified class name should
  be given as a dot-separated (.) or slash-separated (/).
  The former is the regular Java style. The latter allows
  the resulting name to be used directly by native code
  such as JNI (and also in the Jas byte-code compiler classes).
 
*/
public String expandClassName(String name, boolean forJNI) {
 String val = super.expandClassName(name);
  if(forJNI)
     val = val.replace('.', '/');

  return(val);
}  


/**
   @see ConvertibleClassifier.setConvertible(Class, int, boolean)
   @see #setConvertible(String,int,boolean)
 */
public void setConvertible(String className, int match, boolean ok) 
        throws ClassNotFoundException
{
 Class c = findClass(className);
 setConvertible(c, match, ok);
}

public void setConvertible(Class klass, int match, boolean ok) 
{
 getConvertibleClassifier().setConvertible(klass, match, ok);
}


/**
  Retrieves a description (the name) of all the classes that
  the current {@link ConvertibleClassifierInt ConvertibleClassifierInt}
  considers convertible.
  @see #convertibleClassifier
 */
public String[] getConvertibleClasses()
{
 return(getConvertibleClassifier().getConvertibleClasses());
}



public boolean isArray(Object o)
{
  return(o.getClass().isArray());
}  


public Object createArray(String className, int dims)
    throws ClassNotFoundException
{
 int[] d = new int[1];
  d[0] = dims;
  return(createArray(className, d));
}

public Object createArray(String className, int[] dims)
    throws ClassNotFoundException
{
  Class klass = findClass(className);
  return(createArray(klass, dims));
}

public Object createArray(Class type, int[] dims)
    throws ClassNotFoundException
{
  Object val = Array.newInstance(type, dims);

  return(val);
}

public Object getArrayElement(Object o, int index)
{
  int[] d = new int[1];
    d[0] = index;
   return(getArrayElement(o, d));
}

/**
  Get an element from an array, possibly a multi-dimensional
  array 
*/
public Object getArrayElement(Object o, int[] dims)
{
  Object val = o;
  int n = dims.length;

  for(int i = 0 ; i < n ; i++) {
     val = Array.get(val, dims[i]);
  }

  return(val);
}

/**
 Get one or more elements from the specified array,
 treating the indices as entries in the top-level of the
 array rather than as a sequence of entries within a multi-level
 array that identifies a single recursive entry.
*/
public Object getArrayElements(Object o, int[] indices)
      throws Throwable
{
  Object val = o;
  int n = indices.length;

  ExpressionInt e = new org.omegahat.Environment.Parser.Parse.ArrayAccess(o, indices);
  val = e.eval(this);

  return(val);
}



public void setArrayElement(Object o, int index, Object value)
{
  int[] d = new int[1];
    d[0] = index;
   setArrayElement(o, d, value);
}

public void setArrayElement(Object o, int[] dims, Object value)
{
  Object val = o;
  int n = dims.length;
  int i;

  for(i = 0 ; i < n - 1; i++) {
     val = Array.get(val, dims[i]);
  }

  Array.set(val, dims[n-1], value);
}

/*
This isn't needed given that we can call getLength directly ourselves
from S via .Java("java.lang.reflect.Array", "getLength", o)
*/
public int arrayLength(Object o) 
{
  return(Array.getLength(o));
}


public String[] testStringArray() {
 return(new String[4]);
}

public String[] testStringArray(String[] vals) {
  System.err.println("testStringArray: " + vals.length);
 return(vals);
}


public String[] objects(boolean named) {

 Database db;
  if(!named)
    db = anonymousDatabase();
  else
    db = defaultDatabase();

 return(db.objects());
}


public boolean is(Object obj, String klass, boolean isInstance)
    throws ClassNotFoundException
{
 Class k = findClass(klass);
 return(is(obj, k, isInstance));
}

public boolean is(Object obj, Class klass, boolean isInstance)
{
 boolean val;
  if(isInstance) {
    val = klass.isInstance(obj);
  } else
    val = obj.getClass().equals(klass);

 return(val);
}


/**
 A method for allowing an object in the database to be returned
 to the R side with a simple call as well as allowing the
 object to be printed here for debugging purposes.
From R, this can be called something like
<pre>
  .Java(NULL, "identity", ref, .convert = FALSE)
</pre> 
*/
public Object identity(Object o) {
 return(identity(o, false));
}
public Object identity(Object o, boolean print) {
 if(print) {
  System.err.print("[identity] ");
   show(o);
 }

 return(o);
}


native static public boolean nativeSetup(OmegaInterfaceManager This, Object Null);


/**
 Simple function to assign the most recent exception
 from the C code to the evaluator's database.
 This is just to simplify the C code that needs to assign the
 object.
*/
public void assignException(Throwable t)
{
  try {
     assign(".Last.exception", t);
  } catch(Exception e) {
      System.err.println("Failed to assign the previous exception.");
  }
}



public int[][] testIntArray()
{
 int[][] ans;
  ans = new int[3][];
  for(int i = 0; i < ans.length; i++)
   ans[i] = new int[i + 2];
 return(ans);
}

public boolean[][] testBooleanArray()
{
 boolean[][] ans;
  ans = new boolean[3][];
  for(int i = 0; i < ans.length; i++) {
   ans[i] = new boolean[i + 2];
  }

 return(ans);
}

public double[][] testDoubleArray()
{
 double[][] ans;
  ans = new double[3][];
  for(int i = 0; i < ans.length; i++) {
   ans[i] = new double[i + 2];
  }

 return(ans);
}

public boolean[][][] testBooleanArrays()
{
 boolean[][][] ans;
  ans = new boolean[3][4][4];

 return(ans);
}



}


/*
       Copyright (c) 1998, 1999 The Omega Project for Statistical Computing.
          All rights reserved.

*/
/*
                             GNU GENERAL PUBLIC LICENSE

                                Version 2, June 1991

        Copyright (C) 1989, 1991 Free Software Foundation, Inc. 675 Mass
        Ave, Cambridge, MA 02139, USA. Everyone is permitted to copy and
        distribute verbatim copies of this license document, but changing it
        is not allowed.

                                      Preamble

        The licenses for most software are designed to take away your
        freedom to share and change it. By contrast, the GNU General Public
        License is intended to guarantee your freedom to share and change
        free software--to make sure the software is free for all its users.
        This General Public License applies to most of the Free Software
        Foundation's software and to any other program whose authors commit
        to using it. (Some other Free Software Foundation software is
        covered by the GNU Library General Public License instead.) You can
        apply it to your programs, too.

        When we speak of free software, we are referring to freedom, not
        price. Our General Public Licenses are designed to make sure that
        you have the freedom to distribute copies of free software (and
        charge for this service if you wish), that you receive source code
        or can get it if you want it, that you can change the software or
        use pieces of it in new free programs; and that you know you can do
        these things.

        To protect your rights, we need to make restrictions that forbid
        anyone to deny you these rights or to ask you to surrender the
        rights. These restrictions translate to certain responsibilities for
        you if you distribute copies of the software, or if you modify it.

        For example, if you distribute copies of such a program, whether
        gratis or for a fee, you must give the recipients all the rights
        that you have. You must make sure that they, too, receive or can get
        the source code. And you must show them these terms so they know
        their rights.

        We protect your rights with two steps: (1) copyright the software,
        and (2) offer you this license which gives you legal permission to
        copy, distribute and/or modify the software.

        Also, for each author's protection and ours, we want to make certain
        that everyone understands that there is no warranty for this free
        software. If the software is modified by someone else and passed on,
        we want its recipients to know that what they have is not the
        original, so that any problems introduced by others will not reflect
        on the original authors' reputations.

        Finally, any free program is threatened constantly by software
        patents. We wish to avoid the danger that redistributors of a free
        program will individually obtain patent licenses, in effect making
        the program proprietary. To prevent this, we have made it clear that
        any patent must be licensed for everyone's free use or not licensed
        at all.

        The precise terms and conditions for copying, distribution and
        modification follow.

                             GNU GENERAL PUBLIC LICENSE
          TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

        0. This License applies to any program or other work which contains
        a notice placed by the copyright holder saying it may be distributed
        under the terms of this General Public License. The "Program",
        below, refers to any such program or work, and a "work based on the
        Program" means either the Program or any derivative work under
        copyright law: that is to say, a work containing the Program or a
        portion of it, either verbatim or with modifications and/or
        translated into another language. (Hereinafter, translation is
        included without limitation in the term "modification".) Each
        licensee is addressed as "you".

        Activities other than copying, distribution and modification are not
        covered by this License; they are outside its scope. The act of
        running the Program is not restricted, and the output from the
        Program is covered only if its contents constitute a work based on
        the Program (independent of having been made by running the
        Program). Whether that is true depends on what the Program does.

        1. You may copy and distribute verbatim copies of the Program's
        source code as you receive it, in any medium, provided that you
        conspicuously and appropriately publish on each copy an appropriate
        copyright notice and disclaimer of warranty; keep intact all the
        notices that refer to this License and to the absence of any
        warranty; and give any other recipients of the Program a copy of
        this License along with the Program.

        You may charge a fee for the physical act of transferring a copy,
        and you may at your option offer warranty protection in exchange for
        a fee.

        2. You may modify your copy or copies of the Program or any portion
        of it, thus forming a work based on the Program, and copy and
        distribute such modifications or work under the terms of Section 1
        above, provided that you also meet all of these conditions:

        a) You must cause the modified files to carry prominent notices
        stating that you changed the files and the date of any change.

        b) You must cause any work that you distribute or publish, that in
        whole or in part contains or is derived from the Program or any part
        thereof, to be licensed as a whole at no charge to all third parties
        under the terms of this License.

        c) If the modified program normally reads commands interactively
        when run, you must cause it, when started running for such
        interactive use in the most ordinary way, to print or display an
        announcement including an appropriate copyright notice and a notice
        that there is no warranty (or else, saying that you provide a
        warranty) and that users may redistribute the program under these
        conditions, and telling the user how to view a copy of this License.
        (Exception: if the Program itself is interactive but does not
        normally print such an announcement, your work based on the Program
        is not required to print an announcement.)

        These requirements apply to the modified work as a whole. If
        identifiable sections of that work are not derived from the Program,
        and can be reasonably considered independent and separate works in
        themselves, then this License, and its terms, do not apply to those
        sections when you distribute them as separate works. But when you
        distribute the same sections as part of a whole which is a work
        based on the Program, the distribution of the whole must be on the
        terms of this License, whose permissions for other licensees extend
        to the entire whole, and thus to each and every part regardless of
        who wrote it.

        Thus, it is not the intent of this section to claim rights or
        contest your rights to work written entirely by you; rather, the
        intent is to exercise the right to control the distribution of
        derivative or collective works based on the Program.

        In addition, mere aggregation of another work not based on the
        Program with the Program (or with a work based on the Program) on a
        volume of a storage or distribution medium does not bring the other
        work under the scope of this License.

        3. You may copy and distribute the Program (or a work based on it,
        under Section 2) in object code or executable form under the terms
        of Sections 1 and 2 above provided that you also do one of the
        following:

        a) Accompany it with the complete corresponding machine-readable
        source code, which must be distributed under the terms of Sections 1
        and 2 above on a medium customarily used for software interchange;
        or,

        b) Accompany it with a written offer, valid for at least three
        years, to give any third party, for a charge no more than your cost
        of physically performing source distribution, a complete
        machine-readable copy of the corresponding source code, to be
        distributed under the terms of Sections 1 and 2 above on a medium
        customarily used for software interchange; or,

        c) Accompany it with the information you received as to the offer to
        distribute corresponding source code. (This alternative is allowed
        only for noncommercial distribution and only if you received the
        program in object code or executable form with such an offer, in
        accord with Subsection b above.)

        The source code for a work means the preferred form of the work for
        making modifications to it. For an executable work, complete source
        code means all the source code for all modules it contains, plus any
        associated interface definition files, plus the scripts used to
        control compilation and installation of the executable. However, as
        a special exception, the source code distributed need not include
        anything that is normally distributed (in either source or binary
        form) with the major components (compiler, kernel, and so on) of the
        operating system on which the executable runs, unless that component
        itself accompanies the executable.

        If distribution of executable or object code is made by offering
        access to copy from a designated place, then offering equivalent
        access to copy the source code from the same place counts as
        distribution of the source code, even though third parties are not
        compelled to copy the source along with the object code.

        4. You may not copy, modify, sublicense, or distribute the Program
        except as expressly provided under this License. Any attempt
        otherwise to copy, modify, sublicense or distribute the Program is
        void, and will automatically terminate your rights under this
        License. However, parties who have received copies, or rights, from
        you under this License will not have their licenses terminated so
        long as such parties remain in full compliance.

        5. You are not required to accept this License, since you have not
        signed it. However, nothing else grants you permission to modify or
        distribute the Program or its derivative works. These actions are
        prohibited by law if you do not accept this License. Therefore, by
        modifying or distributing the Program (or any work based on the
        Program), you indicate your acceptance of this License to do so, and
        all its terms and conditions for copying, distributing or modifying
        the Program or works based on it.

        6. Each time you redistribute the Program (or any work based on the
        Program), the recipient automatically receives a license from the
        original licensor to copy, distribute or modify the Program subject
        to these terms and conditions. You may not impose any further
        restrictions on the recipients' exercise of the rights granted
        herein. You are not responsible for enforcing compliance by third
        parties to this License.

        7. If, as a consequence of a court judgment or allegation of patent
        infringement or for any other reason (not limited to patent issues),
        conditions are imposed on you (whether by court order, agreement or
        otherwise) that contradict the conditions of this License, they do
        not excuse you from the conditions of this License. If you cannot
        distribute so as to satisfy simultaneously your obligations under
        this License and any other pertinent obligations, then as a
        consequence you may not distribute the Program at all. For example,
        if a patent license would not permit royalty-free redistribution of
        the Program by all those who receive copies directly or indirectly
        through you, then the only way you could satisfy both it and this
        License would be to refrain entirely from distribution of the
        Program.

        If any portion of this section is held invalid or unenforceable
        under any particular circumstance, the balance of the section is
        intended to apply and the section as a whole is intended to apply in
        other circumstances.

        It is not the purpose of this section to induce you to infringe any
        patents or other property right claims or to contest validity of any
        such claims; this section has the sole purpose of protecting the
        integrity of the free software distribution system, which is
        implemented by public license practices. Many people have made
        generous contributions to the wide range of software distributed
        through that system in reliance on consistent application of that
        system; it is up to the author/donor to decide if he or she is
        willing to distribute software through any other system and a
        licensee cannot impose that choice.

        This section is intended to make thoroughly clear what is believed
        to be a consequence of the rest of this License.

        8. If the distribution and/or use of the Program is restricted in
        certain countries either by patents or by copyrighted interfaces,
        the original copyright holder who places the Program under this
        License may add an explicit geographical distribution limitation
        excluding those countries, so that distribution is permitted only in
        or among countries not thus excluded. In such case, this License
        incorporates the limitation as if written in the body of this
        License.

        9. The Free Software Foundation may publish revised and/or new
        versions of the General Public License from time to time. Such new
        versions will be similar in spirit to the present version, but may
        differ in detail to address new problems or concerns.

        Each version is given a distinguishing version number. If the
        Program specifies a version number of this License which applies to
        it and "any later version", you have the option of following the
        terms and conditions either of that version or of any later version
        published by the Free Software Foundation. If the Program does not
        specify a version number of this License, you may choose any version
        ever published by the Free Software Foundation.

        10. If you wish to incorporate parts of the Program into other free
        programs whose distribution conditions are different, write to the
        author to ask for permission. For software which is copyrighted by
        the Free Software Foundation, write to the Free Software Foundation;
        we sometimes make exceptions for this. Our decision will be guided
        by the two goals of preserving the free status of all derivatives of
        our free software and of promoting the sharing and reuse of software
        generally.

                                    NO WARRANTY

        11. BECAUSE THE PROGRAM IS LICENSED FREE OF CHARGE, THERE IS NO
        WARRANTY FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW.
        EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR
        OTHER PARTIES PROVIDE THE PROGRAM "AS IS" WITHOUT WARRANTY OF ANY
        KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO,
        THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
        PARTICULAR PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND
        PERFORMANCE OF THE PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE
        DEFECTIVE, YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR
        CORRECTION.

        12. IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN
        WRITING WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY
        AND/OR REDISTRIBUTE THE PROGRAM AS PERMITTED ABOVE, BE LIABLE TO YOU
        FOR DAMAGES, INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR
        CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OR INABILITY TO USE THE
        PROGRAM (INCLUDING BUT NOT LIMITED TO LOSS OF DATA OR DATA BEING
        RENDERED INACCURATE OR LOSSES SUSTAINED BY YOU OR THIRD PARTIES OR A
        FAILURE OF THE PROGRAM TO OPERATE WITH ANY OTHER PROGRAMS), EVEN IF
        SUCH HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE POSSIBILITY OF
        SUCH DAMAGES.

                            END OF TERMS AND CONDITIONS
*/


