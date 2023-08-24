package io.github.hooksw.konify.runtime.platform

import io.github.hooksw.konify.runtime.platform.Platform
import kotlinx.browser.window

object JsPlatform : Platform {
    override val name: String
        get() = "JS ${window.name}"
}

actual fun getPlatform(): Platform {
    return JsPlatform
}
