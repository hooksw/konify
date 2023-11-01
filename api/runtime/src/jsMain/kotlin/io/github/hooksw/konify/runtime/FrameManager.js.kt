package io.github.hooksw.konify.runtime

import kotlinx.browser.window

actual object FrameManager {
    actual fun postFrameCallback(call: () -> Unit) {
        window.requestAnimationFrame {

        }
    }
}