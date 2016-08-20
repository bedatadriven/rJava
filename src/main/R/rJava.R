

.jinit <- function(classpath = NULL, parameters = getOption("java.parameters"), ...,
     silent = FALSE, force.init = FALSE) {
     
    # NO-OP     
}

.jpackage <- function(name, jars='*', morePaths='', nativeLibrary=FALSE, lib.loc=NULL) {
 
 
}
 
 
.jnew <- function(internalClassName, ..., check = TRUE, silent = !check) {

    if(silent) {
        stop("TODO: silent=TRUE")
    }

    className <- gsub(internalClassName, pattern="/", replacement=".", fixed=TRUE)
    class <- do.call(import, list(as.name(className)))
    constructor <- class$new
    
    do.call(constructor, args = list(...))
}


.jcall <- function(obj, returnSig = "V", method, ..., evalArray = TRUE, 
         evalString = TRUE, check = TRUE, interface = "RcallMethod", 
         simplify = FALSE, use.true.class = FALSE) {
         
    if(typeof(obj) == "character") {
        stop("TODO: static method call")
    
    } else if(typeof(obj) == "externalptr") {
        fn <- do.call("$", list(obj, method))        
        fn(...)
        
    } else {
        stop(sprintf("unsupported typeof(obj) == '%s'", typeof(obj)))
    }
}
