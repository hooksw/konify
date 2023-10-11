package io.github.hooksw.konify.runtime.platform

import org.w3c.dom.HTMLElement
import org.w3c.dom.asList

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual typealias ViewElement = HTMLElement

class JsView(override val view: ViewElement) : PlatformView(view) {
    override fun addView(platformView: PlatformView) {
        view.appendChild(platformView.view)
    }

    override fun removeView(platformView: PlatformView) {
        view.removeChild(platformView.view)
    }

    override fun insertView(platformView: PlatformView, at: Int) {
        view.insertBefore(platformView.view, view.children.item(at))
    }

    override fun index(): Int = view.parentElement!!.children.asList().indexOf(view)

}
