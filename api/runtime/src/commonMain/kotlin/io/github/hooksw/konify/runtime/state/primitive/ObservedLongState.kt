package io.github.hooksw.konify.runtime.state.primitive

internal class ObservedLongState(
    initialValue: Long,
) : MutableLongState {
    private val observers: MutableList<(Long)->Unit> = ArrayList(2)

    override var value: Long = initialValue
        set(value) {
            if (field==value) {
                return
            }
            field = value
            onUpdate(value)
        }


    private fun onUpdate(new: Long) {
        for (observer in observers) {
            observer(new)
        }
    }

    override fun bind(observer: (Long)->Unit) {
        observers.add(observer)
    }
}