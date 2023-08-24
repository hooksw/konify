package io.github.hooksw.konify.complier

import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.name.ClassId

val KEY_ENABLED =
    CompilerConfigurationKey<Boolean>("whether the plugin is enabled")


object KonifyClassIds{

     val View = ClassId.fromString("io.github.hooksw.konify.runtime.annotation.View")
     val ReadOnlyView = ClassId.fromString("io.github.hooksw.konify.runtime.annotation.ReadOnlyView")
}
