package io.github.hooksw.konify.runtime.state

class StaticState<T>(override val value: T) :State<T> {
    override fun bind(observer: (T) -> Unit) {
        observer(value)
    }

    override fun unbind(observer: (T) -> Unit) {

    }
}
fun<T> staticStateOf(value: T)=StaticState(value)