package io.github.hooksw.konify.foundation.style

import androidx.collection.ObjectFloatMap
import io.github.hooksw.konify.foundation.unit.Density
import io.github.hooksw.konify.runtime.utils.fastForEach
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
    val style: Style
    val attr: T
    fun attrEqual(other: T): Boolean {
        return attr == other
    }

    fun update()
}

abstract class CacheAttrStyleNode<T> : StyleNode<T> {
    abstract override var attr: T
    abstract var cacheAttr: T
    abstract fun onUpdate()
    abstract fun updateAttr()
    final override fun update() {
        if (attrEqual(cacheAttr)) {
            updateAttr()
            onUpdate()
        }
    }
}

class Style private constructor(
    private val calls: MutableList<Style.() -> Unit> = ArrayList(2),
    private val nodeList: MutableList<StyleNode<*>> = ArrayList(2)
) {
    internal var density: Density? = null
        private set

    internal fun <T : Any> getOrPutNode(
        factory: () -> StyleNode<T>
    ): StyleNode<T> {
        var exist = false
        var node: StyleNode<T> = factory()
        nodeList.fastForEach {
            if (it::class == node::class) {
                node = it as StyleNode<T>
                exist = true
                return@fastForEach
            }
        }
        if (!exist) {
            nodeList.add(node)
        }
        return node
    }

    operator fun plus(other: Style): Style {
        val newCalls = ArrayList<Style.() -> Unit>(2).apply {
            addAll(calls)
            addAll(other.calls)
        }
        val newNodeList = ArrayList<StyleNode<*>>().apply {
            addAll(nodeList)
            addAll(other.nodeList)
        }.distinctBy { it::class } as MutableList
        return Style(newCalls, newNodeList)
    }

    companion object {
        operator fun invoke(builder: Style.() -> Unit): Style {
            val style = Style()
            style.calls.add(builder)
            return style
        }
    }
}
