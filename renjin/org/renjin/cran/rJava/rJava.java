package org.renjin.cran.rJava;

import org.renjin.eval.EvalException;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.primitives.Native;
import org.renjin.sexp.*;

import java.lang.reflect.Array;

import static org.renjin.cran.rJava.Conversion.toScalarString;
import static org.renjin.gnur.api.Rinternals.*;
import static org.renjin.sexp.SexpType.*;

public class rJava {

  static ClassLoader oClassLoader;

  private static final int maxJavaPars = 20;

  public static SEXP PushToREXP(SEXP clname, SEXP eng, SEXP engCl, SEXP robj, SEXP doConv) {
    throw new UnsupportedOperationException("TODO");
  }

  public static SEXP RJava_checkJVM() {
    return LogicalVector.TRUE;
  }

  public static SEXP RJava_needs_init() {
    return LogicalVector.FALSE;
  }

  public static SEXP RJava_new_class_loader(SEXP p1, SEXP p2) {
    throw new UnsupportedOperationException("TODO");
  }

  public static SEXP RJava_primary_class_loader() {
    return new ExternalPtr<ClassLoader>(Native.currentContext().getSession().getClassLoader());
  }

  public static SEXP RJava_set_class_loader(SEXP loader) {
    throw new UnsupportedOperationException("TODO");
  }

  public static SEXP RJavaCheckExceptions(SEXP silent) {
    // TODO
    return new IntArrayVector(0);
  }


  /**
   * Creates a one dimensional java array
   *
   * @param ar R list or vector
   * @param cl the class name
   */
  public static SEXP RcreateArray(SEXP ar, SEXP cl) {

    if (ar == R_NilValue) {
      return R_NilValue;
    }

    switch (TYPEOF(ar)) {
      case INTSXP: {
        if (ar.inherits("jbyte")) {
          return Conversion.createJArrayRef(Conversion.toByteArray((IntVector) ar), "[B");
        } else if (ar.inherits("jchar")) {
          return Conversion.createJArrayRef(Conversion.toCharArray((IntVector) ar), "[C");
        } else if (ar.inherits("jshort")) {
          return Conversion.createJArrayRef(Conversion.toShortArray((IntVector) ar), "[S");
        } else {
          return Conversion.createJArrayRef(((IntVector) ar).toIntArray(), "[I");
        }
      }
      case REALSXP: {
        if (ar.inherits("jfloat")) {
          return Conversion.createJArrayRef(Conversion.toFloatArray((DoubleVector) ar), "[F");
        } else if (ar.inherits("jlong")) {
          return Conversion.createJArrayRef(Conversion.toLongArray((DoubleVector) ar), "[J");
        } else {
          return Conversion.createJArrayRef(((DoubleVector) ar).toDoubleArray(), "[D");
        }
      }
      case STRSXP:
        return Conversion.createJArrayRef(((StringVector) ar).toArray(), "[Ljava/lang/String;");

      case LGLSXP:
        return Conversion.createJArrayRef(Conversion.toBooleanArray((LogicalVector) ar), "[Z");

      case VECSXP:
        return Conversion.createJArrayRef(Conversion.toObjectArray((ListVector) ar, cl));

      case RAWSXP:
        return Conversion.createJArrayRef(Conversion.toByteArray(((RawVector) ar)), "[B");
    }
    throw new EvalException("Unsupported type to create Java array from.");
  }

  public static SEXP RJava_set_memprof(SEXP fn) {
    throw new UnsupportedOperationException("TODO");
  }

  public static SEXP  RgetBoolArrayCont(SEXP s) { throw new UnsupportedOperationException("TODO"); }
  public static SEXP  RgetByteArrayCont(SEXP s) { throw new UnsupportedOperationException("TODO"); }
  public static SEXP  RgetCharArrayCont(SEXP s) { throw new UnsupportedOperationException("TODO"); }

  public static SEXP  RgetDoubleArrayCont(SEXP s) {
    double[] array = (double[]) ((ExternalPtr) s).getInstance();
    return new DoubleArrayVector(array);
  }

  public static SEXP  RgetFloatArrayCont(SEXP s) { throw new UnsupportedOperationException("TODO"); }

  public static SEXP  RgetIntArrayCont(SEXP s) {
    int[] array = (int[]) ((ExternalPtr) s).getInstance();
    return new IntArrayVector(array);
  }
  public static SEXP  RgetLongArrayCont(SEXP s) { throw new UnsupportedOperationException("TODO"); }

  /**
   * Get contents of the object array in the form of list of ext. pointers
   */
  public static SEXP  RgetObjectArrayCont(SEXP e) {
    Object[] array;

    if (e == R_NilValue) {
      return R_NilValue;
    }

    if (e instanceof ExternalPtr) {
      array = (Object[]) ((ExternalPtr) e).getInstance();
    } else {
      throw new EvalException("invalid object parameter");
    }

    if (array == null) {
      return R_NilValue;
    }

    if (array.length < 1) {
      return R_NilValue;
    }

    ListVector.Builder result = new ListVector.Builder(0, array.length);
    for (Object element : array) {
      result.add(new ExternalPtr<>(element));
    }
    return result.build();
  }

  public static SEXP  RgetShortArrayCont(SEXP s) { throw new UnsupportedOperationException("TODO"); }

  public static SEXP  RgetStringArrayCont(SEXP s) {
    String[] array = (String[]) ((ExternalPtr) s).getInstance();
    return new StringArrayVector(array);
  }

  /** get value of a field of an object or class
   object (int), return signature (string), field name (string)
   arrays and objects are returned as IDs (hence not evaluated)
   class name can be in either form / or .
   */
  public static SEXP  RgetField(SEXP obj, SEXP sig, SEXP name, SEXP trueclass) {

    if (obj == R_NilValue) {
      return R_NilValue;
    }

    FieldAccessBuilder builder = new FieldAccessBuilder();
    builder.findClass(obj, trueclass);
    builder.findField(name, sig);
    return builder.getValue();
  }

  public static SEXP  RsetField(SEXP ref, SEXP name, SEXP value) { throw new UnsupportedOperationException("TODO"); }


  /**
   * Create a NULL external reference
   */
  public static SEXP  RgetNullReference() {
    return new ExternalPtr<Object>(null);
  }

  public static SEXP  RidenticalRef(SEXP ref1, SEXP ref2) {
    ExternalPtr<?> externalPtr1 = (ExternalPtr<?>) ref1;
    ExternalPtr<?> externalPtr2 = (ExternalPtr<?>) ref2;

    if(externalPtr1.getInstance() == externalPtr2.getInstance()) {
      return LogicalVector.TRUE;
    } else {
      return LogicalVector.FALSE;
    }
  }

  public static SEXP RisAssignableFrom(SEXP cl1, SEXP cl2) { throw new UnsupportedOperationException("TODO"); }
  public static SEXP RpollException() { throw new UnsupportedOperationException("TODO"); }
  public static SEXP RthrowException(SEXP ex) { throw new UnsupportedOperationException("TODO"); }


  public static SEXP javaObjectCache(SEXP o, SEXP what) { throw new UnsupportedOperationException("TODO"); }


  // .External

  /**
   *  create new object.
   *  fully-qualified class in JNI notation (string) [, constructor parameters]
   */
  public static SEXP  RcreateObject(SEXP par) {
    SEXP p=par;
    SEXP e;
    int silent=0;
    String clazz;
    Object o;
    Object loader = 0;

    if (!(p instanceof PairList.Node)) {
      throw new EvalException("RcreateObject: invalid parameter");
    }

    p=CDR(p); /* skip first parameter which is the function name */
    e=CAR(p); /* second is the class name */
    if (!IsScalarString(e)) {
      throw new EvalException("RcreateObject: invalid class name");
    }
    clazz = ((StringVector) e).getElementAsString(0);
    p=CDR(p);

    CallBuilder builder = new CallBuilder(maxJavaPars);
    builder.addParameters((PairList) p);

  /* look for named arguments */
    for (PairList.Node node : ((PairList.Node) p).nodes()) {
      if(node.hasTag()) {
        if(node.getName().equals("silent") && isScalarLogical(node.getValue())) {
          silent = ((LogicalVector) node.getValue()).getElementAsInt(0);
        } else if(node.getName().equals("class.loader")) {
          // TODO
        }
      }
    }

    return builder.invokeConstructor(clazz, silent);
  }

  public static boolean IsScalarString(SEXP e) {
    return e instanceof StringVector && e.length() == 1;
  }

  private static boolean isScalarLogical(SEXP value) {
    return value instanceof LogicalVector && value.length() == 1;
  }

  public static SEXP  RgetStringValue(SEXP par) {

    SEXP p=CDR(par);
    SEXP e=CAR(p);
    p=CDR(p);
    if (e==R_NilValue) {
      return R_NilValue;
    }
    String s;
    if (e instanceof ExternalPtr) {
      s =  (String)((ExternalPtr) e).getInstance();
    } else {
      throw new EvalException("invalid object parameter");
    }
    return StringVector.valueOf(s);
  }

  public static SEXP  RinitJVM(SEXP par) { throw new UnsupportedOperationException("TODO"); }

  /**
   * calls .toString() on the passed object (int/extptr) and returns the string
   * value or NULL if there is no toString method
   */
  public static SEXP  RtoString(SEXP par) {
    SEXP p,e,r;
    String s;
    Object o;
    String c;

    p=CDR(par); e=CAR(p); p=CDR(p);
    if (e==R_NilValue) {
      return R_NilValue;
    }
    if (e instanceof ExternalPtr) {
      o = ((ExternalPtr) e).getInstance();
    } else {
      throw new EvalException("RtoString: invalid object parameter");
    }
    if (o == null) {
      return R_NilValue;
    }
    s = o.toString();
    if (s == null) {
      return R_NilValue;
    }

    return StringVector.valueOf(s);
  }

  /**
   * Call specified non-static method on an object
   * object (int), return signature (string), method name (string) [, ..parameters ...]
   * arrays and objects are returned as IDs (hence not evaluated)
   */
  public static SEXP RcallMethod(SEXP par) {
    SEXP p = par;
    SEXP e;
    Object o = null;
    String retsig;
    String mnam;
    String clnam = null;
    Class cls;

    p=CDR(p);
    e=CAR(p);
    p=CDR(p);

    if (e == Null.INSTANCE) {
      throw new EvalException("RcallMethod: call on a NULL object");
    }

    if (e instanceof ExternalPtr) {
      o = ((ExternalPtr) e).getInstance();
    } else if (IsScalarString(e)) {
      clnam = ((StringVector) e).getElementAsString(0);
    } else {
      throw new EvalException("RcallMethod: invalid object parameter");
    }

    if (o == null && clnam == null) {
      throw new EvalException("RcallMethod: attempt to call a method of a NULL object.");
    }

    if (clnam != null) {
      cls = findClass(clnam, oClassLoader);
    } else {
      cls = o.getClass();
    }
    if (cls == null) {
      throw new EvalException("RcallMethod: cannot determine object class");
    }

    e=CAR(p);
    p=CDR(p);

    retsig = toScalarString(e, "return signature parameter");

    e=CAR(p); p=CDR(p);
    mnam = toScalarString(e, "method name");

    CallBuilder callBuilder = new CallBuilder(maxJavaPars);
    callBuilder.addParameters((PairList) p);
    callBuilder.findMethod(cls, mnam);

    return callBuilder.invoke(o, retsig);
  }

  static Class findClass(String clnam) {
    return findClass(clnam, oClassLoader);
  }

  static Class findClass(String clnam, ClassLoader oClassLoader) {
    try {
      return Class.forName(clnam.replace('/', '.'));
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  // .C
  public static void RclearException() { throw new UnsupportedOperationException("TODO"); }

  public static void RuseJNICache(IntPtr flag) { throw new UnsupportedOperationException("TODO"); }

}
