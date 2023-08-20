package com.example.ui.runtime.platform

import android.content.Context
import com.example.ui.runtime.annotation.ReadOnlyView
import com.example.ui.runtime.node.ViewLocal
import com.example.ui.runtime.node.ViewLocalProvider
import com.example.ui.runtime.node.provides
import com.example.ui.runtime.node.viewLocalOf

val LocalContext: ViewLocal<Context> = viewLocalOf { late("Context") }

// -------- Internal --------

@ReadOnlyView
internal fun ProvideIntrinsicLocals(
    context: Context,
    block: @ReadOnlyView () -> Unit
) {
    ViewLocalProvider(
        LocalContext provides context,
        block = block
    )
}

private fun late(name: String): Nothing {
    error("$name is not initialized yet.")
}
