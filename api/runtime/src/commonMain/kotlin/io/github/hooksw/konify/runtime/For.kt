package io.github.hooksw.konify.runtime

import io.github.hooksw.konify.runtime.node.ViewNode
import io.github.hooksw.konify.runtime.signal.Signal


fun <T> ViewNode.For(
    list: Signal<List<T>>,
    key:((T)->Any)?=null,
    child:ViewNode.(Signal<T>)->Unit
){
    
}