package io.github.hooksw.konify.runtime.annotation

// Only injects ViewNode into the parameter list.
@Target(
    AnnotationTarget.FUNCTION,AnnotationTarget.PROPERTY_GETTER
)
@Retention(AnnotationRetention.BINARY)
annotation class ReadOnlyView
