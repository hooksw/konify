package io.github.hooksw.konify.runtime.platform

import io.github.hooksw.konify.runtime.utils.fastForEachIndex

expect abstract class ViewElement

abstract class PlatformView(open val view: ViewElement) {
//    @JvmField
//    val key: Int = getId()
    private val children: MutableList<PlatformView> = mutableListOf()
    abstract fun index(): Int
    abstract fun insertView(platformView: PlatformView, at: Int)
    abstract fun addView(platformView: PlatformView)
    abstract fun removeView(platformView: PlatformView)

    fun diffChildrenWith(list: List<PlatformView>) {
        //children 2 4 5 7
        //new      1 2 4 5 7
        //new      2 4 7
        //new      2 4 5 6
        //new      2 4 7 5
        var i=0
        var newI=0
        while (i<children.size){
            if(children[i]===list[newI]){

            }
            i++
        }
    }

//    companion object {
//        private var id = 0
//        private fun getId(): Int {
//            id += 1
//            return id
//        }
//
//    }
}


