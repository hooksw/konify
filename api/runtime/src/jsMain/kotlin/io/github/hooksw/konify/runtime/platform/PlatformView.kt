package io.github.hooksw.konify.runtime.platform

import org.w3c.dom.HTMLElement

actual class PlatformView(private val element: HTMLElement) {
    actual fun addChild(platformView: PlatformView) {
        element.appendChild(platformView.element)
    }

    actual fun removeChild(platformView: PlatformView) {
        element.removeChild(platformView.element)
    }
}
