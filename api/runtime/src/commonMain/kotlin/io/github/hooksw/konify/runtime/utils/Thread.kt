package io.github.hooksw.konify.runtime.utils

internal expect fun isThreadSafe(): Boolean
internal expect fun post2MainThread(call: () -> Unit)

internal inline fun onMainThread(crossinline call: () -> Unit){
    if(isThreadSafe()){
        call()
    }else{
        post2MainThread{
            call()
        }
    }
}
fun assertOnMainThread(){
    if(!isThreadSafe()) error("You cannot perform this operation outside of the main thread")
}