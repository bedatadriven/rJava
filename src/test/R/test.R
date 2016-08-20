
library(rJava)
library(hamcrest)

test.new <- function() {
    map <- .jnew("java/util/HashMap")
    size <- .jcall(map, "I", "size")
    
    assertThat(size, identicalTo(0L))
}
