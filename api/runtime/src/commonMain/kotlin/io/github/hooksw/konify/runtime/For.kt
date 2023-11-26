package io.github.hooksw.konify.runtime

import io.github.hooksw.konify.runtime.annotation.Component
import io.github.hooksw.konify.runtime.signal.Nodes
import io.github.hooksw.konify.runtime.signal.createComputation
import io.github.hooksw.konify.runtime.signal.memo
import io.github.hooksw.konify.runtime.utils.fastForEach

//todo
@Component
fun <T,R> For(
    list: List<T>,
    key:((T)->R)?=null,
    child:@Component (T)->Unit
){
    val node= Node!!
    if(key==null){
        createComputation {
            node.childNodes?.fastForEach {
                it.cleanup()
            }
            node.childNodes?.clear()
            list.fastForEach(child)
        }.run()
    }else{
        memo {

        }
    }
}