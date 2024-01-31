package io.github.hooksw.konify.compiler.conf

import org.jetbrains.kotlin.config.CompilerConfigurationKey

val KEY_ENABLED= CompilerConfigurationKey.create<Boolean>("enable")
val KEY_SkipSuspendFunction= CompilerConfigurationKey.create<Boolean>("SkipSuspendFunction")
val KEY_SkipSimpleFunction= CompilerConfigurationKey.create<Boolean>("SkipSimpleFunction")