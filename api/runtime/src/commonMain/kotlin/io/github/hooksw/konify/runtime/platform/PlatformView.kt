package io.github.hooksw.konify.runtime.platform

expect class PlatformView {

    fun addChild(platformView: PlatformView)

    fun removeChild(platformView: PlatformView)
}
