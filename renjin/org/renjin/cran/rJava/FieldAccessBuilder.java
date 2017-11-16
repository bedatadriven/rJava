package org.renjin.cran.rJava;

import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

import java.lang.reflect.Field;

import static org.renjin.cran.rJava.Conversion.toScalarString;
import static org.renjin.cran.rJava.Defines.IS_JOBJREF;
import static org.renjin.cran.rJava.rJava.IsScalarString;
import static org.renjin.gnur.api.Rinternals.Rf_asInteger;

class FieldAccessBuilder {

  private Class declaringClass;
  private Object instance = null;
  private Field field;

  private String retsig;
  private String detsig;


  public void findClass(SEXP obj, SEXP trueclass) {
    instance = null;
    String clnam = null;

    int tc = Rf_asInteger(trueclass);

    if ( IS_JOBJREF(obj) ) {
      obj = obj.getAttribute(Symbol.get("jobj"));
    }
    if (obj instanceof ExternalPtr) {
      instance = ((ExternalPtr) obj).getInstance();
    } else if (IsScalarString(obj)) {
      clnam = ((StringVector) obj).getElementAsString(0);
    } else {
      throw new EvalException("invalid object parameter");
    }
    if(instance == null && clnam == null) {
      throw new EvalException("cannot access a field of a NULL object");
    }
    if (instance != null) {
      declaringClass = instance.getClass();
    } else {
      declaringClass = rJava.findClass(clnam, rJava.oClassLoader);
      if (declaringClass == null) {
        throw new EvalException("cannot find class %s", clnam);
      }
    }
    if (declaringClass == null) {
      throw new EvalException("cannot determine object class");
    }
  }

  void findField(SEXP fieldNameSexp, SEXP signatureSexp) {

    String fieldName = toScalarString(fieldNameSexp, "field name");

    try {
      field = declaringClass.getField(fieldName);
    } catch (NoSuchFieldException e) {
      throw new EvalException("No such field '%s' in class '%s'", fieldName, declaringClass.getName());
    }
    if (signatureSexp == Null.INSTANCE) {
      retsig = detsig = Signatures.toSignature(field.getType());
    } else {
      retsig = toScalarString(signatureSexp, "signature parameter");
    }
  }

  public SEXP getValue() {
    Object value;
    try {
      value = field.get(instance);
    } catch (IllegalAccessException e) {
      throw new EvalException("Illegal access to field '%s.%s'", declaringClass.getName(), field.getName());
    }

    if(Signatures.isPrimitive(retsig)) {
      return Conversion.toSexp(value, retsig);
    } else {
      return Conversion.createJObjRef(value, Signatures.getInternalNameFromSignature(retsig));
    }
  }


  /**
   * Returns the name of an object's class (in the form of R string)
   */
  static SEXP getObjectClassName(Object o) {
    if(o == null) {
      return StringVector.valueOf("java/lang/Object");
    }
    return StringVector.valueOf(Signatures.getInternalName(o.getClass()));


  }
}
