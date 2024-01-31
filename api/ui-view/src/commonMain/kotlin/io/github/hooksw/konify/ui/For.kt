package io.github.hooksw.konify.ui

import androidx.collection.mutableScatterMapOf
import io.github.hooksw.konify.foundation.node.Node
import io.github.hooksw.konify.foundation.signal.MutableSignal
import io.github.hooksw.konify.foundation.signal.bind
import io.github.hooksw.konify.foundation.signal.signalOf
import io.github.hooksw.konify.foundation.utils.fastForEach
import io.github.hooksw.konify.foundation.utils.fastForEachIndex
import io.github.hooksw.konify.runtime.annotation.Component
import io.github.hooksw.konify.runtime.node.createNode
import io.github.hooksw.konify.ui.diff.DiffOperation
import io.github.hooksw.konify.ui.diff.DiffUtils

//todo
@Component
fun <T, R> For(
    list: List<T>,
    key: ((T) -> R)? = null,
    fallback: @Component () -> Unit = {},
    child: @Component (Int, T) -> Unit
) {
    createNode { node ->
        var keys = emptyList<R>()
        val nodeIndexMap = mutableScatterMapOf<Node, MutableSignal<Int>>()
        val nodeItemMap = mutableScatterMapOf<Node, MutableSignal<T>>()
        val diff = DiffUtils(object : DiffOperation {
            override fun insert(toBeInsertedIndex: Int, newListItemIndex: Int) {
                createNode(node) {
                    val indexSignal = signalOf(newListItemIndex)
                    val itemSignal = signalOf(list[newListItemIndex])
                    child(indexSignal.value, itemSignal.value)
                    nodeIndexMap[it] = indexSignal
                    nodeItemMap[it] = itemSignal
                    node.useChildNodes().add(toBeInsertedIndex, it)
                }
            }

            override fun remove(index: Int) {
                node.childNodes!!.removeAt(index).also {
                    it.cleanup()
                    nodeIndexMap.remove(it)
                    nodeItemMap.remove(it)
                }
            }

            override fun move(from: Int, to: Int) {
                val n = node.childNodes!!.removeAt(from)
                node.childNodes!!.add(to, n)
            }

        })
        bind {
            if (list.isEmpty()) {
                fallback.invoke()
            } else if (keys.isEmpty() || key == null) {
                node.childNodes?.fastForEach {
                    it.cleanup()
                }
                node.childNodes?.clear()
                list.fastForEachIndex { i, item ->
                    createNode {
                        child(i, item)
                    }
                }
            } else {
                val newKeys = list.map(key)
                diff.perform(keys, newKeys)
                val fastList = list
                node.childNodes!!.fastForEachIndex { index, item ->
                    nodeIndexMap[item]?.value = index
                    nodeItemMap[item]?.value = fastList[index]
                }
                keys = newKeys
            }
        }
    }
}

