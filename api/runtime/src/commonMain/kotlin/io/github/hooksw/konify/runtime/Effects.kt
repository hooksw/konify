package io.github.hooksw.konify.runtime

import io.github.hooksw.konify.runtime.node.ViewNode
import io.github.hooksw.konify.runtime.state.State
import io.github.hooksw.konify.runtime.state.bindWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


fun ViewNode.SideEffect(
    vararg keys: State<*>,
    effect: () -> Unit
) {
    onPrepared(effect)
    for (key in keys) {
        key.bindWithLifecycle(this) {
            effect()
        }
    }
}


fun ViewNode.DisposableEffect(
    vararg keys: State<*>,
    effect: () -> DisposeHandle
) {
    var handle: DisposeHandle? = null
    onPrepared {
        handle = effect()
    }
    onDispose {
        handle?.onDispose()
    }
    for (key in keys) {
        key.bindWithLifecycle(this) {
            handle = effect()
        }
    }
}


fun ViewNode.LaunchedEffect(
    vararg keys: State<*>,
    effect: suspend CoroutineScope.() -> Unit
) {
    val scope = CoroutineScope(Dispatchers.Main.immediate)
    var job: Job? = null
    onPrepared {
        job = scope.launch(block = effect)
    }
    onDispose {
        job?.cancel()
    }
    for (key in keys) {
        key.bindWithLifecycle(this) {
            job?.cancel()
            job = scope.launch(block = effect)
        }
    }
}

fun interface DisposeHandle {
    fun onDispose()
}
