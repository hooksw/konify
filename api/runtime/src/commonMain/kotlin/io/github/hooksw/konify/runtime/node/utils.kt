package io.github.hooksw.konify.runtime.node

import io.github.hooksw.konify.runtime.state.State

fun <T> ViewNode.getCurrent(contextLocal: ViewLocal<T>):State<T>{
    return with(contextLocal){current}
}