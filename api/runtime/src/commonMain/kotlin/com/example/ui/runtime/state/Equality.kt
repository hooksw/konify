package com.example.ui.runtime.state

fun interface Equality<in T> {
    fun compare(old: T, new: T): Boolean
}

fun <T> structuralEquality(): Equality<T> {
    return StructuralEquality
}

fun <T> referentialEquality(): Equality<T> {
    return ReferentialEquality
}

fun <T> nonEqualEquality(): Equality<T> {
    return NonEqualEquality
}

// -------- Internal --------

private object StructuralEquality : Equality<Any?> {
    override fun compare(old: Any?, new: Any?): Boolean {
        return old == new
    }
}

private object ReferentialEquality : Equality<Any?> {
    override fun compare(old: Any?, new: Any?): Boolean {
        return old === new
    }
}

private object NonEqualEquality : Equality<Any?> {
    override fun compare(old: Any?, new: Any?): Boolean {
        return false
    }
}
