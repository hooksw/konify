package io.github.hooksw.konify.runtime.diff

interface DiffOperation {

    fun insert(toOldIndex: Int, fromNewIndex: Int)

    fun remove(index: Int)

    fun move(from: Int, to: Int)
}