package io.github.hooksw.konify.runtime.platform

expect abstract class ViewElement

expect value class PlatformView(val view: ViewElement) {
    fun insertView(platformView: PlatformView, at: Int)
    fun addView(platformView: PlatformView)

    fun removeFromParent()
}

