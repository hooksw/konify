package io.github.hooksw.konify.runtime.node

import io.github.hooksw.konify.runtime.reactive.Computation
import io.github.hooksw.konify.runtime.reactive.currentReactiveSystem
import io.github.hooksw.konify.runtime.utils.fastForEach

interface ReusableNode : Node {
    fun onCache(fn: () -> Unit)
    fun onReuse(fn: () -> Unit)
}

@PublishedApi
internal class ReusableNodeImpl<T>(nodeValue: T) : NodeImpl<T>(nodeValue), ReusableNode {


    private var onCache: MutableList<() -> Unit>? = null


    private var onReuse: MutableList<() -> Unit>? = null


    override fun onCache(fn: () -> Unit) {
        if (onCache == null) onCache = ArrayList(3)
        onCache!!.add(fn)
    }

    override fun onReuse(fn: () -> Unit) {
        if (onReuse == null) onReuse = ArrayList(3)
        onReuse!!.add(fn)
    }

    //lifecycle
    fun cache() {
        if (state != LifecycleState.Prepared) {
            error("This ViewNode is not prepared.")
        }
        onCache?.fastForEach {
            it()
        }
        state = LifecycleState.Inactive
    }

    fun reuse() {
        if (state != LifecycleState.Inactive) {
            error("This ViewNode is not Inactive.")
        }
        onReuse?.fastForEach {
            it()
        }
        state = LifecycleState.Prepared
    }

    init {
        addOnDispose {
            onCache = null
            onReuse = null
        }
    }

}
