package org.renjin.cran.rJava;

class Signatures {

  public static String toSignature(Class<?> type) {
    StringBuilder s = new StringBuilder();
    appendTypeToSignature(s, type);
    return s.toString();
  }

  public static boolean isPrimitive(String signature) {
    switch (signature.charAt(0)) {
      case 'B':
      case 'Z':
      case 'C':
      case 'S':
      case 'I':
      case 'J':
      case 'F':
      case 'D':
        return true;
      default:
        return false;
    }
  }

  private static void appendTypeToSignature(StringBuilder s, Class<?> type) {
    if(type.equals(byte.class)) {
      s.append('B');
    } else if(type.equals(boolean.class)) {
      s.append('Z');
    } else if(type.equals(char.class)) {
      s.append('C');
    } else if(type.equals(short.class)) {
      s.append('S');
    } else if(type.equals(int.class)) {
      s.append('I');
    } else if(type.equals(long.class)) {
      s.append('J');
    } else if(type.equals(float.class)) {
      s.append('F');
    } else if(type.equals(double.class)) {
      s.append('D');
    } else if(type.isArray()) {
      s.append('[');
      appendTypeToSignature(s, type.getComponentType());
    } else {
      s.append("L");
      s.append(getInternalName(type));
      s.append(';');
    }
  }

  static String getInternalName(Class<?> type) {
    Class enclosingClass = type.getEnclosingClass();
    if(enclosingClass == null) {
      return type.getName().replace('.', '/');
    } else {
      return enclosingClass.getName().replace('.', '/') + "$" + type.getName();
    }
  }

  public static String getInternalClassNameOf(Object object) {
    if(object == null) {
      return "java/lang/Object";
    }
    return getInternalName(object.getClass());
  }

  public static String getInternalNameFromSignature(String signature) {
    if(!(signature.startsWith("L") && signature.endsWith(";"))) {
      throw new IllegalArgumentException("Expected object descriptor: " + signature);
    }
    return signature.substring(1, signature.length() - 1);
  }
}
