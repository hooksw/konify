package io.github.hooksw.konify.runtime.utils

import kotlinx.browser.window

actual fun post2NextFrame(call: () -> Unit) {
    window.requestAnimationFrame {
        call()
    }
}