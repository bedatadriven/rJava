
if(identical(R.Version()$engine, "Renjin")) {

    .onLoad <- function(libname, pkgname) {
      library.dynam("rJava", pkgname, libname)
      # pass on to the system-independent part
      .jfirst(libname, pkgname)
    }

}
