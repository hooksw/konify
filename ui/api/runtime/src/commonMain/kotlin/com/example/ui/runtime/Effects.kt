package com.example.ui.runtime

import com.example.ui.runtime.annotation.ReadOnlyView
import com.example.ui.runtime.state.State

@ReadOnlyView
fun Effect(
    vararg keys: State<*>,
    effect: () -> Unit
) {
    val node = currentViewNode
    node.onPrepared(effect)
    for (key in keys) {
        key.bind {
            effect()
        }
    }
}

@ReadOnlyView
fun DisposableEffect(
    vararg keys: State<*>,
    effect: () -> DisposeHandle
) {
    val node = currentViewNode
    var handle: DisposeHandle? = null
    node.onPrepared {
        handle = effect()
    }
    node.onDispose {
        handle?.onDispose()
    }
    for (key in keys) {
        key.bind {
            handle = effect()
        }
    }
}

fun interface DisposeHandle {
    fun onDispose()
}
