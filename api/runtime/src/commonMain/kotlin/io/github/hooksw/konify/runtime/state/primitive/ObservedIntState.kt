package io.github.hooksw.konify.runtime.state.primitive

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
        for (observer in observers) {
            observer(new)
        }
    }

    override fun bind(observer: (Int)->Unit) {
        observers.add(observer)
    }
}