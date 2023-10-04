package io.github.hooksw.konify.runtime.style

import io.github.hooksw.konify.runtime.annotion.Once
import io.github.hooksw.konify.runtime.annotion.StyleBuilder
import io.github.hooksw.konify.runtime.unit.Density
import io.github.hooksw.konify.runtime.platform.PlatformView
import io.github.hooksw.konify.runtime.state.State
import kotlin.reflect.KClass

/*
* style顺序：
* 1. build :when merged, get a copied style rather original style
* 2. initial to view
* 3. add observer to platform ViewNode scope
*
* style设计
* 1. 可拓展（自定义）
* 2. 延迟绑定
* 3. 相同的style，后面的覆盖前面的
*    * 需要一个标识方法识别不同的style，但style是用类属性定义的，要如何设计？
* */

interface StyleNode<T> {
    val attr: T
    fun update(density: Density,platformView: PlatformView)
}

@Once
class Style private constructor(
    private val extraNodes: Map<KClass<out StyleNode<*>>, StyleNode<*>> =
        extraDefaultNodes.entries.associate { it.key to it.value.invoke() },
    internal val bindMap: MutableMap<String, State<*>> = hashMapOf()
) {
    internal val paddingStyleNode= PaddingStyleNode()
    var ready = false
        internal set
    var platformView: PlatformView? = null
        private set
    var density:Density? = null
        private set

    fun <T : StyleNode<*>> extraNodeFor(nodeKClass: KClass<T>): T {
        val node = extraNodes[nodeKClass]
            ?: error("this kind of node [${nodeKClass.simpleName}] should be registered first.")
        return (node as T)
    }

    fun merge(builder: @StyleBuilder Style.() -> Unit): Style {
        return this.apply(builder)
    }

    operator fun plus(other: Style): Style {
        return Style(extraNodes + other.extraNodes, (bindMap + other.bindMap) as MutableMap<String, State<*>>)
    }

    companion object {
        private val extraDefaultNodes = hashMapOf<KClass<out StyleNode<*>>, () -> StyleNode<*>>()
        fun <T : StyleNode<*>> registerDefaultNode(nodeType: KClass<T>, factory: () -> T) {
            if (extraDefaultNodes[nodeType] == null) {
                extraDefaultNodes[nodeType] = factory
            } else {
                error("this type of style node [${nodeType.simpleName}] is already register!")
            }
        }

        val default by lazy {
            Style()
        }

        operator fun invoke(builder: @StyleBuilder Style.() -> Unit): Style {
            val style = Style()
            Style().builder()
            return style
        }
    }
}
