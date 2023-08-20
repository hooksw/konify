package com.example.ui.runtime.annotation

// Only injects ViewNode into the parameter list.
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.TYPE,
    AnnotationTarget.TYPE_PARAMETER
)
@Retention(AnnotationRetention.BINARY)
annotation class ReadOnlyView
