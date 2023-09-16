package io.github.hooksw.konify.runtime.platform

import android.view.View
import android.view.ViewGroup

actual class PlatformView(internal val view: View) {
    actual fun appendChild(platformView: PlatformView) {
        val childView = platformView.view
        if (view !is ViewGroup) {
            error("Cannot add $childView to $view, because the later is not a ViewGroup")
        }
        view.addView(platformView.view)
    }

    actual fun removeChild(platformView: PlatformView) {
        val childView = platformView.view
        if (view !is ViewGroup) {
            error("Cannot add $childView to $view, because the later is not a ViewGroup")
        }
        view.removeView(childView)
    }
}
