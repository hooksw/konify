package com.example.ui.runtime.platform

expect class PlatformView {
    fun addChild(platformView: PlatformView)

    fun removeChild(platformView: PlatformView)
}
