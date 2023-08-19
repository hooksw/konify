package com.example.ui

expect class PlatformView {
    fun addChild(platformView: PlatformView)

    fun removeChild(platformView: PlatformView)
}