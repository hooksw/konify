package io.github.hooksw.konify.runtime.platform

import android.view.View
import android.view.ViewGroup

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual typealias ViewElement = View

@JvmInline
actual value class PlatformView(val view: ViewElement) {
    actual fun addView(platformView: PlatformView) {
        val childView = platformView.view
        if (view !is ViewGroup) {
            error("Cannot add $childView to $view, because the later is not a ViewGroup")
        }
        view.addView(platformView.view)
    }

    actual fun removeFromParent() {
        if (view.parent!=null&&view.parent is ViewGroup) {
            (view.parent as ViewGroup).removeView(view)
        }
    }

    actual fun insertView(platformView: PlatformView, at: Int) {
        (view as ViewGroup).addView(platformView.view,at)
    }
}
