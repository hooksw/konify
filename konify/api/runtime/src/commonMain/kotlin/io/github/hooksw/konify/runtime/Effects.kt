package io.github.hooksw.konify.runtime

import io.github.hooksw.konify.runtime.annotation.ReadOnlyView
import io.github.hooksw.konify.runtime.state.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@ReadOnlyView
fun SideEffect(
    vararg keys: State<*>,
    effect: () -> Unit
) {
    val node = currentViewNode
    node.onPrepared(effect)
    for (key in keys) {
        key.bindWithLifecycle {
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
        key.bindWithLifecycle {
            handle = effect()
        }
    }
}

@ReadOnlyView
fun LaunchedEffect(
    vararg keys: State<*>,
    effect: suspend CoroutineScope.() -> Unit
) {
    val node = currentViewNode
    val scope = CoroutineScope(Dispatchers.Main.immediate)
    var job: Job? = null
    node.onPrepared {
        job = scope.launch(block = effect)
    }
    node.onDispose {
        job?.cancel()
    }
    for (key in keys) {
        key.bindWithLifecycle {
            job?.cancel()
            job = scope.launch(block = effect)
        }
    }
}

fun interface DisposeHandle {
    fun onDispose()
}
