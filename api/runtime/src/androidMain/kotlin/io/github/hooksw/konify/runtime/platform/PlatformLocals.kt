package io.github.hooksw.konify.runtime.platform

import android.content.Context
import io.github.hooksw.konify.runtime.annotation.ReadOnlyView
import io.github.hooksw.konify.runtime.annotation.View
import io.github.hooksw.konify.runtime.node.ViewLocal
import io.github.hooksw.konify.runtime.node.ViewLocalProvider
import io.github.hooksw.konify.runtime.node.provides
import io.github.hooksw.konify.runtime.node.viewLocalOf

val LocalContext: ViewLocal<Context> = viewLocalOf { late("Context") }

// -------- Internal --------

@ReadOnlyView
internal fun ProvideIntrinsicLocals(
    context: Context,
    block: @View () -> Unit
) {
    ViewLocalProvider(
        LocalContext provides context,
        block = block
    )
}

private fun late(name: String): Nothing {
    error("$name is not initialized yet.")
}
