package io.github.hooksw.konify.runtime.state

internal class ObservedState<T>(
    initialValue: T,
    private val equality: Equality<T>
) : MutableState<T> {
    private val observers: MutableList<Observer<T>> = ArrayList(2)

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
            observer.accept(new)
        }
    }

    override fun bind(observer: Observer<T>) {
        observers.add(observer)
    }

    override fun unbind(observer: Observer<T>) {
        observers.remove(observer)
    }
}
