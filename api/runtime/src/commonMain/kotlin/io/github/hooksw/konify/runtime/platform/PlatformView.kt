package io.github.hooksw.konify.runtime.platform


expect  class PlatformView {

    fun children(): List<PlatformView>
    fun insertView(platformView: PlatformView, at: Int)
    fun addView(platformView: PlatformView)
    fun removeView(toveRemoved:PlatformView)

}


