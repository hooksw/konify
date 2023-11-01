package io.github.hooksw.konify.runtime.local

import io.github.hooksw.konify.runtime.node.ViewNode
import io.github.hooksw.konify.runtime.signal.Signal

fun <T> ViewNode.getCurrent(contextLocal: ViewLocal<T>):Signal<T>{
    return with(contextLocal){current}
}