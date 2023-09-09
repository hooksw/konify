package io.github.hooksw.konify.runtime.state.primitive

internal class ObservedDoubleState(
    initialValue: Double,
) : MutableDoubleState {
    private val observers: MutableList<(Double)->Unit> = ArrayList(2)

    override var value: Double = initialValue
        set(value) {
            if (field==value) {
                return
            }
            field = value
            onUpdate(value)
        }


    private fun onUpdate(new: Double) {
        for (observer in observers) {
            observer(new)
        }
    }

    override fun bind(observer: (Double)->Unit) {
        observers.add(observer)
    }

    override fun unbind(observer: (Double) -> Unit) {
        observers.remove (observer)
    }
}