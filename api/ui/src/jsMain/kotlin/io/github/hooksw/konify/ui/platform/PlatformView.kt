package io.github.hooksw.konify.ui.platform

import org.w3c.dom.Element

actual  class PlatformView(val view: Element) {
    actual fun addView(platformView: PlatformView) {
        view.appendChild(platformView.view)
    }

    actual fun removeView(toveRemoved: PlatformView) {
        view.removeChild(toveRemoved.view)
    }

    actual fun insertView(platformView: PlatformView, at: Int) {
        view.insertBefore(platformView.view, view.children.item(at))
    }

    actual inline fun foreachChild(action: (PlatformView) -> Unit) {
        var ele = view.firstElementChild
        while (ele != null) {
            action(PlatformView(ele))
            ele = ele.nextElementSibling
        }
    }

}