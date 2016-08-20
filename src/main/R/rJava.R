

.jinit <- function(classpath = NULL, parameters = getOption("java.parameters"), ...,
     silent = FALSE, force.init = FALSE) {
     
    # NO-OP     
}

.jpackage <- function(name, jars='*', morePaths='', nativeLibrary=FALSE, lib.loc=NULL) {
 
 
}
 
importInternal <- function(internalClassName) {
    className <- gsub(internalClassName, pattern="/", replacement=".", fixed=TRUE)
    class <- do.call(import, list(as.name(className)))
    class
}
 
.jnew <- function(class, ..., check = TRUE, silent = !check) {

    if(silent) {
        stop("TODO: silent=TRUE")
    }

    classDef <- importInternal(class)
    constructor <- classDef$new
    
    do.call(constructor, args = list(...))
}


.jcall <- function(obj, returnSig = "V", method, ..., evalArray = TRUE, 
         evalString = TRUE, check = TRUE, interface = "RcallMethod", 
         simplify = FALSE, use.true.class = FALSE) {
         
    if(typeof(obj) == "character") {
        class <- importInternal(obj)
        fn <- do.call("$", list(class, method))
        fn(...)
        
    } else if(typeof(obj) == "externalptr") {
        fn <- do.call("$", list(obj, method))        
        fn(...)
        
    } else {
        stop(sprintf("unsupported typeof(obj) == '%s'", typeof(obj)))
    }
}
