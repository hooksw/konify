package io.github.hooksw.konify.runtime.utils

actual fun isMainThread(): Boolean {
    return true
}

actual inline fun post2MainThread(call: () -> Unit) {
    call()
}

internal actual class Lock {
    actual inline fun <T> read(call: () -> T): T {
      return  call()
    }

    actual inline fun write(call: () -> Unit) {
        call()
    }
}
