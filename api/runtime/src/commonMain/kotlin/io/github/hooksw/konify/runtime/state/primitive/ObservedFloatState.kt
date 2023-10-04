package io.github.hooksw.konify.runtime.state.primitive

import io.github.hooksw.konify.runtime.utils.fastForEach

internal class ObservedFloatState(
    initialValue: Float,
) : MutableFloatState {
    private val observers: MutableList<(Float)->Unit> = ArrayList(2)

    override var value: Float = initialValue
        set(value) {
            if (field==value) {
                return
            }
            field = value
            onUpdate(value)
        }


    private fun onUpdate(new: Float) {
        observers.fastForEach {observer->
            observer(new)
        }
    }

    override fun bind(observer: (Float)->Unit) {
        observers.add(observer)
    }
    override fun unbind(observer: (Float) -> Unit) {
        observers.remove (observer)
    }
}