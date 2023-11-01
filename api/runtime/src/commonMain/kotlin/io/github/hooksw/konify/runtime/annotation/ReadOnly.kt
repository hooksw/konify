package io.github.hooksw.konify.runtime.annotation

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
annotation class ReadOnly()