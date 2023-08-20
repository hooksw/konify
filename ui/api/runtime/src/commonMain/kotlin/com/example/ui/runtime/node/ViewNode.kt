package com.example.ui.runtime.node

import com.example.ui.runtime.platform.PlatformView
import com.example.ui.runtime.state.State

sealed interface ViewNode {
    // -------- Hierarchy --------

    fun createChildNode(): ViewNode

    // -------- Platform --------

    fun registerPlatformView(platformView: PlatformView)

    // -------- Lifecycle --------

    fun prepare()

    fun onPrepared(block: () -> Unit)

    fun onDispose(block: () -> Unit)

    // -------- ViewLocal --------

    fun <T> getViewLocal(viewLocal: ViewLocal<T>): State<T>?

    fun provideViewLocal(providedViewLocal: ProvidedViewLocal<*>)
}
