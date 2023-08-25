package io.github.hooksw.konify.runtime.state

fun interface Observer<in T> {
    fun accept(value: T)
}
