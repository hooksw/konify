package io.github.hooksw.konify.runtime.annotation

// Only injects ViewNode into the parameter list.
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPE,
    AnnotationTarget.TYPE_PARAMETER
)
@Retention(AnnotationRetention.BINARY)
annotation class ReadOnlyView
