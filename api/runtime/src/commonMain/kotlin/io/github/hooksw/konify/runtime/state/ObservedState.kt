package io.github.hooksw.konify.runtime.state

internal class ObservedState<T>(
    initialValue: T,
    private val equality: Equality<T>
) : MutableState<T> {
    private val observers: MutableList<(T)->Unit> = ArrayList(2)

    override var value: T = initialValue
        set(value) {
            if (equality.compare(field, value)) {
                return
            }
            field = value
            onUpdate(value)
        }

    private fun onUpdate(new: T) {
        for (observer in observers) {
            observer(new)
        }
    }

    override fun bind(observer: (T)->Unit) {
        observers.add(observer)
    }

    override fun unbind(observer: (T)->Unit) {
        observers.remove(observer)
    }
}
