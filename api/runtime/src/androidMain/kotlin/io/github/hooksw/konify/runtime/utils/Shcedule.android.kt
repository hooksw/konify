package io.github.hooksw.konify.runtime.utils

import android.view.Choreographer

actual fun post2NextFrame(call: () -> Unit) {
    Choreographer.getInstance().postFrameCallback {
        call()
    }
}