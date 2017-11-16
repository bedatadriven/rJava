package org.renjin.cran.rJava;


import org.renjin.sexp.SEXP;

class Defines {

  public static boolean IS_JOBJREF(SEXP obj) {
    return obj.inherits("jobjRef") || obj.inherits("jarrayRef") || obj.inherits("jrectRef");
  }

  public static boolean IS_JARRAYREF(SEXP obj) {
    return obj.inherits("jobjRef") || obj.inherits("jarrayRef") || obj.inherits("jrectRef");
  }

  public static boolean IS_JRECTREF(SEXP obj) {
    return obj.inherits("jrectRef");
  }

}
