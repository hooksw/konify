package io.github.hooksw.konify.complier

import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

val KEY_ENABLED: CompilerConfigurationKey<Boolean> = CompilerConfigurationKey("whether the plugin is enabled")

object KonifyIds {
    const val AnnotationPackageString: String = "io.github.hooksw.konify.runtime.annotation"

    val AnnotationPackageFqName: FqName = FqName(AnnotationPackageString)

    const val ViewString: String = "$AnnotationPackageString.View"

    val ViewFqName: FqName = FqName(ViewString)

    val ViewClassId = ClassId.fromString(ViewString)

    const val ReadOnlyViewString: String = "$AnnotationPackageString.ReadOnlyView"

    val ReadOnlyViewFqName: FqName = FqName(ReadOnlyViewString)

    val ReadOnlyViewClassId = ClassId.fromString(ReadOnlyViewString)
}
