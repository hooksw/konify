package io.github.hooksw.konify.runtime.local

import io.github.hooksw.konify.runtime.node.ViewNode
import io.github.hooksw.konify.runtime.state.State

fun <T> ViewNode.getCurrent(contextLocal: ViewLocal<T>):State<T>{
    return with(contextLocal){current}
}