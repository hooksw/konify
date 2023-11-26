package io.github.hooksw.konify.ui.platform


expect class PlatformView {
    inline fun foreachChild(action: (PlatformView) -> Unit)
    fun insertView(platformView: PlatformView, at: Int)
    fun addView(platformView: PlatformView)
    fun removeView(toveRemoved: PlatformView)
}


