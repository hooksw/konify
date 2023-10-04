package io.github.hooksw.konify.runtime.state.primitive


interface MutableIntState {
    var value: Int

    fun bind(observer: (Int) -> Unit)

    fun unbind(observer: (Int) -> Unit)
}

interface MutableLongState {
    var value: Long

    fun bind(observer: (Long) -> Unit)

    fun unbind(observer: (Long) -> Unit)
}

interface MutableFloatState {
    var value: Float

    fun bind(observer: (Float) -> Unit)

    fun unbind(observer: (Float) -> Unit)
}

interface MutableDoubleState {
    var value: Double

    fun bind(observer: (Double) -> Unit)

    fun unbind(observer: (Double) -> Unit)
}


fun mutableIntStateOf(
    initialValue: Int,
): MutableIntState {
    return ObservedIntState(
        initialValue = initialValue
    )
}

fun mutableLongStateOf(
    initialValue: Long,
): MutableLongState {
    return ObservedLongState(
        initialValue = initialValue
    )
}

fun mutableFloatStateOf(
    initialValue: Float,
): MutableFloatState {
    return ObservedFloatState(
        initialValue = initialValue
    )
}

fun mutableDoubleStateOf(
    initialValue: Double,
): MutableDoubleState {
    return ObservedDoubleState(
        initialValue = initialValue
    )
}