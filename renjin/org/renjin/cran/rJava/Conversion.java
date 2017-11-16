package org.renjin.cran.rJava;

import org.renjin.eval.EvalException;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gnur.api.Rinternals;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.sexp.*;

import java.lang.reflect.Array;

import static org.renjin.gnur.api.Rinternals.R_do_slot_assign;

class Conversion {

  static String toScalarString(SEXP sexp, String parameterName) {
    if (sexp instanceof StringVector && sexp.length() == 1) {
      return ((StringVector) sexp).getElementAsString(0);
    }
    throw new EvalException("invalid " + parameterName);
  }

  static float[] toFloatArray(DoubleVector e) {
    float[] array = new float[e.length()];
    for (int i = 0; i < array.length; i++) {
      array[i] = (float) e.getElementAsDouble(i);
    }
    return array;
  }

  static long[] toLongArray(DoubleVector e) {
    long[] array = new long[e.length()];
    for (int i = 0; i < array.length; i++) {
      array[i] = (long) e.getElementAsDouble(i);
    }
    return array;
  }

  static char[] toCharArray(IntVector e) {
    char[] array = new char[e.length()];
    for (int i = 0; i < array.length; i++) {
      array[i] = (char) e.getElementAsInt(i);
    }
    return array;
  }

  static byte[] toByteArray(IntVector e) {
    byte[] array = new byte[e.length()];
    for (int i = 0; i < array.length; i++) {
      array[i] = (byte) e.getElementAsInt(i);
    }
    return array;
  }

  static byte[] toByteArray(RawVector e) {
    byte[] array = new byte[e.length()];
    for (int i = 0; i < array.length; i++) {
      array[i] = e.getElementAsByte(i);
    }
    return array;
  }

  static short[] toShortArray(IntVector e) {
    short[] array = new short[e.length()];
    for (int i = 0; i < array.length; i++) {
      array[i] = (short) e.getElementAsInt(i);
    }
    return array;
  }

  static Object[] toObjectArray(ListVector ar, SEXP cl) {
    Class ac = Object.class;
    String sigName = null;

    for (SEXP e : ar) {
      if (e != Null.INSTANCE &&
          !e.inherits("jobjRef") &&
          !e.inherits("jarrayRef") &&
          !e.inherits("jrectRef")) {
        throw new EvalException("Cannot create a Java array from a list that contains anything other than Java object references.");
      }
    }
      /* optional class name for the objects contained in the array */
    if (cl instanceof StringVector && cl.length() > 0) {
      String cname = ((StringVector) cl).getElementAsString(0);
      if (cname != null) {
        ac = rJava.findClass(cname);

	    /* it's valid to have [* for class name (for mmulti-dim
	       arrays), but then we cannot add [L..; */
        if (cname.charAt(0) == '[') {
	      /* we have to add [ prefix to the signature */
          sigName = "[" + cname;
        } else {
          sigName = "[L" + cname + ";";
        }
      }
    }

    Object[] a = (Object[]) Array.newInstance(ac, ar.length());
    for (int i = 0; i < ar.length(); i++) {
      SEXP e = ar.getElementAsSEXP(i);
      Object o = 0;
      if (e != Null.INSTANCE) {
        SEXP sref = e.getAttribute(Symbol.get("jobj"));
        if (sref instanceof ExternalPtr) {
          o = ((ExternalPtr) sref).getInstance();
        }
      }
      a[i] = o;
    }

    return a;
  }

  static boolean[] toBooleanArray(LogicalVector e) {
    boolean[] array = new boolean[e.length()];
    for (int i = 0; i < array.length; i++) {
      array[i] = e.getElementAsRawLogical(0) != 0;
    }
    return array;
  }

  static SEXP toSexp(Object result, String returnSignature) {
    switch (returnSignature.charAt(0)) {
      case 'V':
        return Null.INSTANCE;

      case 'I':
        return IntArrayVector.valueOf((Integer) result);

      case 'B':
        return IntArrayVector.valueOf((Byte) result);

      case 'C':
        return IntArrayVector.valueOf((Character) result);

      case 'J':
        return new DoubleArrayVector((Long) result);

      case 'S':
        return IntArrayVector.valueOf((Short) result);

      case 'Z':
        return LogicalVector.valueOf((Boolean) result);

      case 'D':
        return DoubleVector.valueOf((Double) result);

      case 'F':
        return DoubleVector.valueOf((Float) result);

      case 'L':
      case '[':
        if(result == null) {
          return Null.INSTANCE;
        } else {
          return new ExternalPtr<Object>(result);
        }

      default:
        throw new EvalException("Unsupported/invalid signature " + returnSignature);
    }
  }

  static SEXP createJObjRef(Object object) {
    return createJObjRef(object, Signatures.getInternalClassNameOf(object));
  }

  /**
   * Creates a new jobjRef object. If klass is NULL then the class is determined from the object
   * (if also o=NULL then the class is set to java/lang/Object)
   */
  static SEXP createJObjRef(Object object, String internalClassName) {
    SEXP classDef = Rinternals.R_do_MAKE_CLASS(BytePtr.nullTerminatedString("jobjRef", Charsets.UTF_8));
    SEXP oo = Rinternals.R_do_new_object(classDef);

    if (!oo.inherits("jobjRef")) {
      throw new EvalException("unable to create jobjRef object");
    }

    if (internalClassName == null) {
      internalClassName = Signatures.getInternalClassNameOf(object);
    }

    R_do_slot_assign(oo, Symbol.get("jobj"), new ExternalPtr<>(object));
    R_do_slot_assign(oo, Symbol.get("jclass"), StringVector.valueOf(internalClassName));

    return oo;
  }

  static SEXP createJArrayRef(Object array) {
    return createJArrayRef(array, Signatures.getInternalName(array.getClass()));
  }

  static SEXP createJArrayRef(Object array, String signature) {

    SEXP classDef = Rinternals.R_do_MAKE_CLASS(BytePtr.nullTerminatedString("jarrayRef", Charsets.UTF_8));
    SEXP oo = Rinternals.R_do_new_object(classDef);

    R_do_slot_assign(oo, Symbol.get("jobj"), new ExternalPtr<>(array));
    R_do_slot_assign(oo, Symbol.get("jclass"), StringVector.valueOf(signature));
    R_do_slot_assign(oo, Symbol.get("jsig"), StringVector.valueOf(signature));

    return oo;
  }


}
