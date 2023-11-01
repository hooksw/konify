package io.github.hooksw.konify.runtime.platform

import android.view.View
import android.view.ViewGroup


actual class PlatformView(val view: View) {
    actual fun addView(platformView: PlatformView) {
        val childView = platformView.view
        if (view !is ViewGroup) {
            error("Cannot add $childView to $view, because the later is not a ViewGroup")
        }
        view.addView(platformView.view)
    }

    actual fun removeView(toveRemoved:PlatformView){
        (view as ViewGroup).removeView(toveRemoved.view)
    }

    actual fun insertView(platformView: PlatformView, at: Int) {
        (view as ViewGroup).addView(platformView.view, at)
    }

    actual fun children(): List<PlatformView> = buildList {
        val view = view as ViewGroup
        for (i in 0..<view.childCount) {
            add(PlatformView(view.getChildAt(i)))
        }
    }
}
