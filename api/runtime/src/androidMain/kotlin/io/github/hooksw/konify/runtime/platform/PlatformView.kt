package io.github.hooksw.konify.runtime.platform

import android.view.View
import android.view.ViewGroup
import java.util.ArrayList

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual typealias ViewElement = View

class AndroidView(override val view: ViewElement) : PlatformView(view) {
    override fun addView(platformView: PlatformView) {
        val childView = platformView.view
        if (view !is ViewGroup) {
            error("Cannot add $childView to $view, because the later is not a ViewGroup")
        }
        view.addView(platformView.view)
    }

    override fun removeView(platformView: PlatformView) {
        (view as ViewGroup).removeView(platformView.view)
    }

    override fun insertView(platformView: PlatformView, at: Int) {
        (view as ViewGroup).addView(platformView.view, at)
    }

    override fun index(): Int = (view.parent as ViewGroup).indexOfChild(view)
}
