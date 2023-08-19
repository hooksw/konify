package com.example.ui

abstract class State<T> {
    abstract val value: T

    protected val observers = hashMapOf<Observer<T>, Lifecycle>()

    @ReadOnlyViewNode
    fun bind(viewNode: ViewNode, observer: Observer<T>) {
        observers[observer] = viewNode.lifecycle
        viewNode.lifecycle.addOnMount {
            observer.accept(value)
        }
        viewNode.lifecycle.addOnCleanup {
            observers.remove(observer)
        }
    }
}

class ReadonlyState<T>(override val value: T) : State<T>()

class MutableState<T>(
    init: T
) : State<T>() {
    override var value: T = init
        set(value) {
            if (value == field) return
            field = value
            observers.forEach { (observer, lifecycle) ->
                if (lifecycle.state == LifecycleState.Mounted) {
                    observer.accept(field)
                }
            }
        }

}


fun <T> stateOf(i: T): MutableState<T> {
    return MutableState(i)
}

fun <T> readonlyStateOf(i: T): ReadonlyState<T> {
    return ReadonlyState(i)
}


fun interface Observer<T> {
    @ReadOnlyViewNode
    fun accept(call: T)
}
