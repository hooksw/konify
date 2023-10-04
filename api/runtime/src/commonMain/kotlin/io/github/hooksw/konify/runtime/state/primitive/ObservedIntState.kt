package io.github.hooksw.konify.runtime.state.primitive

import io.github.hooksw.konify.runtime.utils.fastForEach

internal class ObservedIntState(
    initialValue: Int,
) : MutableIntState {
    private val observers: MutableList<(Int)->Unit> = ArrayList(2)

    override var value: Int = initialValue
        set(value) {
            if (field==value) {
                return
            }
            field = value
            onUpdate(value)
        }


    private fun onUpdate(new: Int) {
        observers.fastForEach {observer->
            observer(new)
        }
    }

    override fun bind(observer: (Int)->Unit) {
        observers.add(observer)
    }
    override fun unbind(observer: (Int) -> Unit) {
        observers.remove (observer)
    }
}