package io.github.hooksw.konify.runtime.utils

expect fun isMainThread(): Boolean
expect fun post2MainThread(call: () -> Unit)

inline fun onMainThread(crossinline call: () -> Unit){
    if(isMainThread()){
        call()
    }else{
        post2MainThread{
            call()
        }
    }
}