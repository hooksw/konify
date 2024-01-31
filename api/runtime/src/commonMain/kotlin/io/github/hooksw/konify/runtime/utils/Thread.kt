package io.github.hooksw.konify.runtime.utils

internal expect fun isMainThread(): Boolean
internal expect fun post2MainThread(call: () -> Unit)

fun assertOnMainThread(){
    if(!isMainThread()) error("You cannot perform this operation outside of the main thread")
}

internal expect class Lock() {
    inline fun <T> read(call: () -> T): T
    inline fun write(call: () -> Unit)
}