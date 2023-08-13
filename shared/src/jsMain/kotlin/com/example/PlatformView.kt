package com.example

import org.w3c.dom.HTMLElement

actual class PlatformView(private val view: HTMLElement) {
    actual fun addChild(platformView: PlatformView) {
        view.appendChild(platformView.view)
    }

    actual fun removeChild(platformView: PlatformView){
        view.removeChild(platformView.view)
    }
}
