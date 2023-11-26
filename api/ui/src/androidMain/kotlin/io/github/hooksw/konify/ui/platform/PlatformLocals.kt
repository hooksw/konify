package io.github.hooksw.konify.ui.platform

import android.content.Context
import io.github.hooksw.konify.runtime.local.ContextLocal
import io.github.hooksw.konify.runtime.local.contextLocalOf

val LocalContext: ContextLocal<Context> = contextLocalOf { late("Context") }

// -------- Internal --------


private fun late(name: String): Nothing {
    error("$name is not initialized yet.")
}
