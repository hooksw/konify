package io.github.hooksw.konify.foundation.modifier

import io.github.hooksw.konify.runtime.utils.fastForEach

interface ModifierHandler {
    fun handle(modifier: Modifier)
    fun onHandleFinish(modifier: Modifier) {
        (modifier as? ModifierImpl)?.foreachElement {
            if (it is AttrElement<*>) {
                it.dirty = false
            }
        }
    }
}

interface Modifier {
    interface Element {
        val modifier: Modifier
    }

    companion object : Modifier {
        internal fun initialWith(element: Element): Modifier {
            return ModifierImpl(element)
        }
    }
}

internal class ModifierImpl(private val first: Modifier.Element) : Modifier {
    private val eleList = mutableListOf(first)

    fun appendElement(element: Modifier.Element) {
        eleList.add(element)
    }

    inline fun foreachElement(call: (Modifier.Element) -> Unit) {
        eleList.fastForEach(call)
    }

    var handler: ModifierHandler? = null
}

internal infix fun Modifier.append(element: Modifier.Element): Modifier {
    return if (this == Modifier) Modifier.initialWith(element)
    else (this as ModifierImpl).apply { appendElement(element) }
}


internal interface StaticElement : Modifier.Element {

}

internal interface ObserverElement : Modifier.Element {
    val observeCall: () -> Unit
}

abstract class AttrElement<T>(override val modifier: Modifier, val update: () -> Unit) :
    ObserverElement {
    internal var dirty: Boolean = false
    internal abstract fun createAttr(): T
    internal val finalAttr: T by lazy(LazyThreadSafetyMode.NONE) { createAttr() }
    val activeAttr: T by lazy(LazyThreadSafetyMode.NONE) { createAttr() }
    internal abstract fun T.updateFrom(other: T)

    final override val observeCall: () -> Unit = {
        update()
        if (finalAttr != activeAttr) {
            if (!dirty) {
                dirty = true
                (modifier as? ModifierImpl)?.handler?.handle(modifier)
            }
        }
    }
}
