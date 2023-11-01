package io.github.hooksw.konify.runtime

expect object FrameManager {
    fun postFrameCallback(call:()->Unit)
}