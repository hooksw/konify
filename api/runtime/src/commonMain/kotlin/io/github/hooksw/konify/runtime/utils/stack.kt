package io.github.hooksw.konify.runtime.utils

import kotlin.jvm.JvmInline


@JvmInline
value class CheckStack<T>(private val list: MutableList<T> = mutableListOf()) {
    fun currentOrNull(): T? = list.lastOrNull()
    fun push(item: T) {
        list.add(item)
    }

    fun pop(): T {
        return list.removeLastOrNull() ?: error("Stack is empty")
    }

    fun popSelf(item: T) {
        check(pop() === item)
    }
}