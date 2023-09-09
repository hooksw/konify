package io.github.hooksw.konify.runtime.platform

import android.content.Context
import io.github.hooksw.konify.runtime.node.*

val LocalContext: ViewLocal<Context> = viewLocalOf { late("Context") }

// -------- Internal --------


private fun late(name: String): Nothing {
    error("$name is not initialized yet.")
}
