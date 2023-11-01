package io.github.hooksw.konify.runtime.utils

actual fun isMainThread(): Boolean {
    return true
}

actual  fun post2MainThread(call: () -> Unit) {
    call()
}