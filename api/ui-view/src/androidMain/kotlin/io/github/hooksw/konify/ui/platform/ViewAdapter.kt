package io.github.hooksw.konify.ui.platform

import android.app.Activity
import android.view.ViewGroup
import android.widget.LinearLayout
import io.github.hooksw.konify.foundation.local.ViewLocalProvider
import io.github.hooksw.konify.foundation.local.provides

fun Activity.setContent(children:  () -> Unit) {
    val root =
    val frameLayout = LinearLayout(this).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        orientation=LinearLayout.VERTICAL
        tag = root
    }
    val nativeView = NativeView(frameLayout)
    root.registerNativeView(nativeView)
    root.ViewLocalProvider(LocalContext provides this){
        children()
    }
    setContentView(frameLayout)
    root.prepare()
}
