package io.github.hooksw.konify.runtime

import io.github.hooksw.konify.runtime.node.ViewNode
import io.github.hooksw.konify.runtime.state.State


fun <T> ViewNode.For(
    list: State<List<T>>,
    key:((T)->Any)?=null,
    child:ViewNode.(State<T>)->Unit
){
    
}