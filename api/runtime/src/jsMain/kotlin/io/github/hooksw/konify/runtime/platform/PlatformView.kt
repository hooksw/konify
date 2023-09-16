package io.github.hooksw.konify.runtime.platform

import org.w3c.dom.HTMLElement

actual class PlatformView(val element: HTMLElement) {
    actual fun appendChild(platformView: PlatformView) {
        element.appendChild(platformView.element)
    }

    actual fun removeChild(platformView: PlatformView) {
        element.removeChild(platformView.element)
    }
}
