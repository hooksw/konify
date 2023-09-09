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

internal class ObservedIntState(
    initialValue: Int,
    private val equality: Equality<Int>
) : MutableIntState {
    private val observers: MutableList<Observer<Int>> = ArrayList(2)

    override var intValue: Int = initialValue
        set(value) {
            if (equality.compare(field, value)) {
                return
            }
            field = value
            onUpdate(value)
        }

    private fun onUpdate(new: Int) {
        for (observer in observers) {
            observer.accept(new)
        }
    }

    override fun bind(observer: Observer<Int>) {
        observers.add(observer)
    }

    override fun unbind(observer: Observer<Int>) {
        observers.remove(observer)
    }
}
