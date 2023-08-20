package com.example.ui.runtime.state

fun interface Observer<in T> {
    fun accept(value: T)
}
