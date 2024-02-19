package io.github.hooksw.konify.runtime.reactive

import io.github.hooksw.konify.runtime.utils.fastForEach


//listeners

private val Updates = mutableListOf<Computation>()

private var batching = false
internal fun pushUpdate(call: Computation) {
    if (batching) {
        Updates.add(call)
    } else {
        call.run()
    }
}

internal inline fun batch(call: () -> Unit) {
    batching = true
    call()
    Updates.fastForEach {
        it.run()
    }
    Updates.clear()
    batching = false
}
