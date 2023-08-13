package com.example

import android.view.View
import android.view.ViewGroup
import java.util.ArrayList

actual class PlatformView(private val view: View) {
    actual fun addChild(platformView: PlatformView) {
        (view as ViewGroup).addView(view)
    }

    actual fun removeChild(platformView: PlatformView) {
        (view as ViewGroup).addView(view)
    }

}
