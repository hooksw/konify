package io.github.hooksw.konify.runtime

import io.github.hooksw.konify.runtime.annotation.Component
import io.github.hooksw.konify.runtime.annotation.ReadOnly
import io.github.hooksw.konify.runtime.reactive.CurrentNode
import io.github.hooksw.konify.runtime.reactive.createComputation
import io.github.hooksw.konify.runtime.reactive.untrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Component
@ReadOnly
fun SideEffect(
    vararg keys: Any,
    effect: () -> Unit
) {

    effectCommon(keys,
        block = effect,
        onDispose = {}
    )
}

@Component
@ReadOnly
fun DisposableEffect(
    vararg keys: Any,
    effect: () -> DisposeHandle
) {
    var handle: DisposeHandle? = null

    effectCommon(keys,
        block = {
            handle?.onDispose()
            handle = effect()
        },
        onDispose = {
            handle?.onDispose()
        }
    )
}


@Component
@ReadOnly
fun LaunchedEffect(
    vararg keys: Any,
    effect: suspend CoroutineScope.() -> Unit
) {
    val scope = CoroutineScope(Dispatchers.Main.immediate)
    var job: Job? = null
    effectCommon(keys,
        block = {
            job?.cancel()
            job = scope.launch(block = effect)
        },
        onDispose = {
            job?.cancel()
        }
    )
}

private fun effectCommon(
    keys: Array<out Any>,
    block: () -> Unit,
    onDispose: () -> Unit
) {
    val node = CurrentNode
    node?.addOnMount(block)
    node?.addOnDispose(onDispose)
    val keys = keys as Array<() -> Any>
    createComputation {
        keys.forEach { it() }
        untrack {
            block()
        }
    }
}

fun interface DisposeHandle {
    fun onDispose()
}
