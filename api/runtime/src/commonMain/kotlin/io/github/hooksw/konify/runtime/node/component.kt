package io.github.hooksw.konify.runtime.node

fun ViewNode.component(builder:ViewNode.()->Unit){
    val node=createChild()
    node.builder()
    node.prepare()
}