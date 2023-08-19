package com.example.ui

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.experimental.ExperimentalTypeInference


interface EffectResult {
    fun clean()
}

object EffectScope {
    inline fun onClean(crossinline call: () -> Unit) = object : EffectResult {
        override fun clean() {
            call()
        }

    }
}


@ReadOnlyViewNode
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
fun <T> cleanableEffect(
    viewNode: ViewNode,
    vararg key: MutableState<T>,
    effect: EffectScope.() -> EffectResult
) {

    var result: EffectResult?
    observeAll(viewNode, *key) {
        result = EffectScope.effect()
    }

    viewNode.lifecycle.addOnMount {
        result = EffectScope.effect()
        viewNode.lifecycle.addOnCleanup {
            result?.clean()
        }
        //TODO
    }
}

@ReadOnlyViewNode
fun <T> createEffect(
    viewNode: ViewNode,
    vararg key: MutableState<T>,
    effect: suspend EffectScope.() -> Unit
) {
    var job: Job? = null
    observeAll(viewNode, *key) {
        job?.cancel()
        job = viewNode.lifecycle.scope.launch {
            EffectScope.effect()
        }
    }
    viewNode.lifecycle.addOnMount {
        job = viewNode.lifecycle.scope.launch {
            EffectScope.effect()
        }
    }
}

@ReadOnlyViewNode
private inline fun <T> observeAll(
    viewNode: ViewNode,
    vararg key: MutableState<T>,
    crossinline function: () -> Unit
) {
    key.forEach {
        it.bind(viewNode) { _ ->
            function()
        }
    }
}