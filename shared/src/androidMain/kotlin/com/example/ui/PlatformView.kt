package com.example.ui

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

actual class PlatformView(private val view: View) {
    actual fun addChild(platformView: PlatformView) {
        (view as ViewGroup).addView(platformView.view)
        Log.e("PlatformView","$view add ${platformView.view}")
    }

    actual fun removeChild(platformView: PlatformView) {
        (view as ViewGroup).addView(platformView.view)
    }

}

@ViewMarker
actual fun Text(viewNode: ViewNode, text: String) {
    val node = viewNode.newNode()
    node.registerNativeView(PlatformView(TextView(AndroidContext).apply {
        setText(text)
    }))
}

@ViewMarker
actual fun Row(
    viewNode: ViewNode,
    children: @ViewMarker (ViewNode) -> Unit
) {
    val node = viewNode.newNode()
    node.registerNativeView(PlatformView(LinearLayout(AndroidContext).apply {
        this.orientation = LinearLayout.HORIZONTAL
    }))
    children(node)
}

fun Activity.host(children: @ViewMarker (ViewNode) -> Unit) {
    val frameLayout = FrameLayout(AndroidContext).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
    val node = ViewNode.empty()
    frameLayout.tag=node
    node.registerNativeView(PlatformView(frameLayout))
    children(node)
    setContentView(frameLayout)
}