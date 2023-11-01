package io.github.hooksw.konify.runtime

import io.github.hooksw.konify.runtime.node.ViewNode
import io.github.hooksw.konify.runtime.signal.*
import io.github.hooksw.konify.runtime.signal.Owners
import io.github.hooksw.konify.runtime.utils.fastForEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


fun ViewNode.SideEffect(
    vararg keys: Any,
    effect: () -> Unit
) {

    effectCommon(keys,
        block = effect,
        onDispose = {}
    )
}

fun ViewNode.DisposableEffect(
    vararg keys:Any,
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


fun ViewNode.LaunchedEffect(
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
//as we don't need auto track here,so not use signalConsume
private fun ViewNode.effectCommon(
    keys: Array<out Any>,
     block: () -> Unit,
     onDispose: () -> Unit
) {
    val owner= Owners.last()
    registerPrepared(block)
    keys.fastForEach {
        if(it is StateObserver){
            it.observers.add(block)
            if(it is Owner){
                it.stateDisposerMap[it]=onDispose
            }else{
                owner.stateDisposerMap[it]=onDispose
            }
        }
    }
}

fun interface DisposeHandle {
    fun onDispose()
}
