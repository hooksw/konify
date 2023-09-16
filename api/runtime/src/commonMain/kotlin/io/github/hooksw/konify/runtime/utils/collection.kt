package io.github.hooksw.konify.runtime.utils

internal inline fun <T> List<T>.fastForEach(action: (item:T) -> Unit) {
    for (i in 0 until size) {
        action(this[i])
    }
}
internal inline fun <T> List<T>.fastForEachIndex(action: (index:Int,item:T) -> Unit) {
    for (i in 0 until size) {
        action(i,this[i])
    }
}