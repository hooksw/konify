package io.github.hooksw.konify.ui.platform

import android.content.Context
import io.github.hooksw.konify.runtime.context.ContextLocal
import io.github.hooksw.konify.runtime.context.contextLocalOf
import io.github.hooksw.konify.runtime.context.staticContextLocalOf

val LocalContext: ContextLocal<Context> =
    staticContextLocalOf { error("LocalContext is not initialized yet.") }
