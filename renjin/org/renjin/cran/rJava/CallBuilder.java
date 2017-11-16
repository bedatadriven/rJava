package org.renjin.cran.rJava;

import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.renjin.cran.rJava.Defines.IS_JARRAYREF;

class CallBuilder {

  private static final int maxJavaPars = 20;

  private Class[] parameterTypes = new Class[maxJavaPars];
  private Object[] parameters = new Object[maxJavaPars];
  private int parameterIndex = 0;
  private int maxpars;
  private Method method;

  public CallBuilder(int maxpars) {
    this.maxpars = maxpars;
  }

  public void addParameters(PairList parameterList) {

    for (PairList.Node node : parameterList.nodes()) {
    /* skip all named arguments */
      if (node.hasTag()) {
        continue;
      }
      if (parameterIndex >= maxpars) {
        if (maxpars == maxJavaPars) {
          throw new EvalException("Too many arguments in Java call. maxJavaPars is %d, recompile rJava with higher number if needed.", maxJavaPars);
        }
        break;
      }

      SEXP e = node.getValue();

      if (e instanceof StringVector) {
        addStringParameter((StringVector) e);

      } else if (e instanceof RawVector) {
        addRawParameter((RawVector) e);

      } else if (e instanceof IntVector) {
        addIntParameter((IntVector) e);

      } else if (e instanceof DoubleVector) {
        addDoubleParameter((DoubleVector) e);

      } else if (e instanceof LogicalVector) {
        addLogicalVector((LogicalVector) e);

      } else if (e instanceof ListVector || e instanceof S4Object) {
        if (Defines.IS_JOBJREF(e)) {
          Object o = null;
          String jc = null;
          SEXP n = e.getNames();
          if (!(n instanceof StringVector)) {
            n = null;
          }
          if (e instanceof ListVector && e.length() == 1) {
            throw new EvalException("Old, unsupported S3 Java object encountered.");
          } else { /* new objects are S4 objects */
            SEXP sref;
            sref = e.getAttribute(Symbol.get("jobj"));
            if (sref instanceof ExternalPtr) {
              o = ((ExternalPtr) sref).getInstance();
            } else { /* if jobj is anything else, assume NULL ptr */
              o = null;
            }
            addParameter(o, findParameterType(e));
          }
        }
      }
    }

    parameters = Arrays.copyOf(this.parameters, parameterIndex);
    parameterTypes = Arrays.copyOf(this.parameterTypes, parameterIndex);
  }


  private Class<?> findParameterType(SEXP e) {
    String jc = null;
    SEXP sclass = e.getAttribute(Symbol.get("jclass"));
    if (sclass instanceof StringVector && sclass.length() == 1) {
      jc = ((StringVector) sclass).getElementAsString(0);
    }
    if (IS_JARRAYREF(e) && jc != null && jc.length() == 0) {
	    /* if it's jarrayRef with jclass "" then it's an uncast array - use sig instead */
      sclass = e.getAttribute(Symbol.get("jsig"));
      if (sclass instanceof StringVector && sclass.length() == 1) {
        jc = ((StringVector) sclass).getElementAsString(0);
      }
    }
    if (jc != null) {
      if (!jc.startsWith("[")) { /* not an array, we assume it's an object of that class */
        return findClass(jc);
      } else {/* array signature is passed as-is */
        return signatureToClass(jc);
      }
    }
    return Object.class;
  }

  private Class<?> signatureToClass(String jc) {
    switch (jc.charAt(0)) {
      case 'Z':
        return boolean.class;
      case 'B':
        return byte.class;
      case 'C':
        return char.class;
      case 'S':
        return short.class;
      case 'I':
        return int.class;
      case 'J':
        return long.class;
      case 'F':
        return float.class;
      case 'D':
        return double.class;
      case 'L':
        int endOfClassName = jc.indexOf(';');
        String className = jc.substring(1, endOfClassName);
        return findClass(className);
      case '[':
        return findClass(jc.replace('/', '.'));
      default:
        throw new IllegalArgumentException("Invalid signature: " + jc);
    }
  }

  private Class<?> findClass(String jc) {
    try {
      return Class.forName(jc.replace('/', '.'));
    } catch (ClassNotFoundException e) {
      throw new EvalException("Cannot find class: " + jc, e);
    }
  }

  private void addLogicalVector(LogicalVector e) {
    if(e.length() == 1) {
      addParameter(e.getElementAsRawLogical(0) != 0, boolean.class);
    } else {
      addParameter(Conversion.toBooleanArray(e));
    }
  }

  private void addIntParameter(IntVector e) {
    if(e.length() == 1) {
      Integer value = e.getElementAsInt(0);
      if(e.inherits("jbyte")) {
        addParameter(value.byteValue(), byte.class);
      } else if(e.inherits("jchar")) {
        addParameter((char)value.intValue(), char.class);
      } else if(e.inherits("jshort")) {
        addParameter(value.shortValue(), short.class);
      } else {
        addParameter(value, int.class);
      }
    } else {
      if(e.inherits("jbyte")) {
        addParameter(Conversion.toByteArray(e));
      } else if(e.inherits("jchar")) {
        addParameter(Conversion.toCharArray(e));
      } else if(e.inherits("jshort")) {
        addParameter(Conversion.toShortArray(e));
      } else {
        addParameter(e.toIntArray());
      }
    }
  }


  private void addDoubleParameter(DoubleVector e) {
    if(e.length() == 1) {
      Double value = e.getElementAsDouble(0);
      if(e.inherits("jfloat")) {
        addParameter(value.floatValue(), float.class);
      } else if(e.inherits("jlong")) {
        addParameter(value.longValue(), long.class);
      } else {
        addParameter(value, double.class);
      }
    } else {
      if(e.inherits("jfloat")) {
        addParameter(Conversion.toFloatArray(e));
      } else if(e.inherits("jlong")) {
        addParameter(Conversion.toLongArray(e));
      } else {
        addParameter(e.toDoubleArray());
      }
    }
  }


  private void addStringParameter(StringVector e) {
    if(e.length() == 1) {
      addParameter(e.getElementAsString(0));
    } else {
      addParameter(e.toArray(), String[].class);
    }
  }


  private void addRawParameter(RawVector e) {
    addParameter(e.toByteArray(), byte[].class);
  }

  private void addParameter(Object value) {
    addParameter(value, value.getClass());

  }

  private void addParameter(Object value, Class<?> valueClass) {
    parameters[parameterIndex] = value;
    parameterTypes[parameterIndex] = valueClass;
    parameterIndex++;
  }

  void findMethod(Class cls, String methodName) {
    try {
      this.method = cls.getMethod(methodName, parameterTypes);
    } catch (NoSuchMethodException e) {
      throw new EvalException(String.format("Could not find method '%s' with signature %s in class %s",
          methodName,
          Arrays.toString(parameterTypes),
          cls.getName()));
    }
  }

  public SEXP invoke(Object o, String retsig) {
    Object result;
    try {
      result = method.invoke(o, parameters);
    } catch (Exception e) {
      throw new EvalException("Exception invoking method " + method + ": " + e.getMessage(), e);
    }

    return Conversion.toSexp(result, retsig);
  }


  public SEXP invokeConstructor(String clazz, int silent) {
    Constructor<?> constructor;
    try {
      constructor = findClass(clazz).getConstructor(parameterTypes);
    } catch (NoSuchMethodException e) {
      throw new EvalException("Could not find constructor matching the signature: " + Arrays.toString(parameterTypes));
    }
    Object newObject;
    try {
      newObject = constructor.newInstance(parameters);
    } catch (Exception e) {
      throw new EvalException("Exception invoking constructor", e);
    }
    return new ExternalPtr<>(newObject);
  }
}
