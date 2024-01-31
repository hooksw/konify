package io.github.hooksw.konify.ui.diff

interface DiffOperation {

    fun insert(toBeInsertedIndex: Int, newListItemIndex: Int)

    fun remove(index: Int)

    fun move(from: Int, to: Int)
}