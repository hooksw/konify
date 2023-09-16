package io.github.hooksw.konify.runtime.state

class ConstantState<T>(override val value: T) :State<T> {
    override fun bind(observer: (T) -> Unit) {
        observer(value)
    }

    override fun unbind(observer: (T) -> Unit) {
        observer(value)
    }
}
fun<T> constant(value: T)=ConstantState(value)