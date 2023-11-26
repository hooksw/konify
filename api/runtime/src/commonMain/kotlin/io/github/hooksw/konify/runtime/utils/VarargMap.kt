package io.github.hooksw.konify.runtime.utils


inline fun <reified T> Array<() -> T>.invokeMap(): Array<T> {
    return Array(size) {
        get(it).invoke()
    }
}