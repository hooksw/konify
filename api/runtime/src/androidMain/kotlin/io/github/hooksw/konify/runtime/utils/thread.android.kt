package io.github.hooksw.konify.runtime.utils

import android.os.Handler
import android.os.Looper

actual fun isThreadSafe(): Boolean {
    return Looper.getMainLooper().isCurrentThread
}

private val handler = Handler(Looper.getMainLooper())

actual fun post2MainThread(call: () -> Unit) {
    handler.post(call)
}