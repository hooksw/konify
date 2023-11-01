package io.github.hooksw.konify.runtime.signal

class ConstantSignal<T>(override val value: T) : Signal<T>

fun <T> constant(value: T) = ConstantSignal(value)