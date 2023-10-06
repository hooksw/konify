package io.github.hooksw.konify.runtime.platform

import org.w3c.dom.HTMLElement
import org.w3c.dom.asList

actual typealias ViewElement = HTMLElement

actual value class PlatformView(val view: ViewElement) {
    actual fun addView(platformView: PlatformView) {
        view.appendChild(platformView.view)
    }

    actual fun removeFromParent() {
        view.parentElement?.removeChild(view)
    }

    actual fun insertView(platformView: PlatformView, at: Int) {
        view.insertBefore(platformView.view, view.children.item(at))
    }

    actual fun index(): Int= view.parentElement!!.children.asList().indexOf(view)

}
