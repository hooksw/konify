package io.github.hooksw.konify.runtime

import android.view.Choreographer

actual object FrameManager {
    actual fun postFrameCallback(call: () -> Unit) {
        Choreographer.FrameCallback {
            call()
        }
    }
}