package io.github.hooksw.konify.runtime.state.primitive

import io.github.hooksw.konify.runtime.state.MutableState


interface MutableIntState : MutableState<Int> {
    override var value: Int
}

interface MutableLongState : MutableState<Long> {
    override var value: Long
}

interface MutableFloatState : MutableState<Float> {
    override var value: Float
}

interface MutableDoubleState : MutableState<Double> {
    override var value: Double
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