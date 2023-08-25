package io.github.hooksw.konify.runtime.platform

import android.os.Build
import io.github.hooksw.konify.runtime.platform.Platform

object AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform {
    return AndroidPlatform
}
