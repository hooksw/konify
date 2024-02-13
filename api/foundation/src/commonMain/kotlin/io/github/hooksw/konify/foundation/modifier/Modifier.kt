package io.github.hooksw.konify.foundation.modifier

interface ModifierHandler {
    fun handle(modifier: Modifier)
}

interface Modifier {
    interface Element {
        val modifier: Modifier
        var next: Element?
    }

    companion object : Modifier {
        internal fun initialWith(element: Element): Modifier {
            return ModifierImpl(element)
        }
    }
}

internal class ModifierImpl(val first: Modifier.Element) : Modifier {
    internal var last: Modifier.Element = first
    lateinit var handler: ModifierHandler
}

internal infix fun Modifier.append(element: Modifier.Element): Modifier {
    return if (this == Modifier) Modifier.initialWith(element)
    else (this as ModifierImpl).apply { element.next = element; last = element }
}


internal interface StaticElement : Modifier.Element {

}

internal interface ObserverElement : Modifier.Element {
    fun observeCall()
}

abstract class AttrElement<T>(override val modifier: Modifier, val update: () -> Unit) :
    ObserverElement {
    override var next: Modifier.Element? = null
    var dirty: Boolean = false
    abstract fun createAttr(): T
    internal val finalAttr: T by lazy(LazyThreadSafetyMode.NONE) { createAttr() }
    val activeAttr: T by lazy(LazyThreadSafetyMode.NONE) { createAttr() }
    abstract fun T.updateFrom(other: T)


    final override fun observeCall() {
        update()
        if (finalAttr == activeAttr) {
            return
        }
        if (!dirty) {
            dirty = true
            (modifier as? ModifierImpl)?.handler?.handle(modifier)
        }
    }
}
