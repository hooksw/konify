package io.github.hooksw.konify.runtime.annotation

// Injects ViewNode into the parameter list, and creates a child node in place.
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPE,
    AnnotationTarget.TYPE_PARAMETER
)
@Retention(AnnotationRetention.BINARY)
annotation class View
