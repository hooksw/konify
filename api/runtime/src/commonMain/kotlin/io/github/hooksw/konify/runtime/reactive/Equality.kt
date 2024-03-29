package io.github.hooksw.konify.runtime.reactive

fun interface Equality<in T> {
    fun compare(old: T?, new: T?): Boolean
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
fun  arrayEquality(): Equality<Array<*>> {
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

private object ArrayEquality : Equality<Array<*>> {
    override fun compare(old: Array<*>?, new: Array<*>?): Boolean {
        return old.contentEquals(new)
    }
}
