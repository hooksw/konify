package io.github.hooksw.konify.runtime.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastForEach(action: (item: T) -> Unit) {
    contract {
        callsInPlace(action)
    }
    for (i in indices) {
        action(this[i])
    }
}

@OptIn(ExperimentalContracts::class)
internal inline fun <T> Array<T>.fastForEach(action: (item: T) -> Unit) {
    contract {
        callsInPlace(action)
    }
    for (i in indices) {
        action(this[i])
    }
}


@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastForEachIndex(action: (index: Int, item: T) -> Unit) {
    contract {
        callsInPlace(action)
    }
    for (i in indices) {
        action(i, this[i])
    }
}