package com.example.ui.runtime.platform

import kotlinx.browser.window

object JsPlatform : Platform {
    override val name: String
        get() = "JS ${window.name}"
}

actual fun getPlatform(): Platform {
    return JsPlatform
}
