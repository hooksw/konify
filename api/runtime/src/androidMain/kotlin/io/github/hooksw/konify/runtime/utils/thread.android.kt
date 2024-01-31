package io.github.hooksw.konify.runtime.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

actual fun isMainThread(): Boolean {
    return Looper.getMainLooper().isCurrentThread
}

private val handler = Handler(Looper.getMainLooper())

actual fun post2MainThread(call: () -> Unit) {
    handler.post(call)
}

internal actual class Lock {
    internal val readWriteLock = ReentrantReadWriteLock()
    actual inline fun <T> read(call: () -> T): T {
        return readWriteLock.read(call)
    }

    actual inline fun write(call: () -> Unit) {
        readWriteLock.write(call)
    }
}
