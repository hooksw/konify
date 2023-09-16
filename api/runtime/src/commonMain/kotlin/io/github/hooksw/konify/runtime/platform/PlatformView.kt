package io.github.hooksw.konify.runtime.platform

expect class PlatformView {

    fun appendChild(platformView: PlatformView)

    fun removeChild(platformView: PlatformView)
}
