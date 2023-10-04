package io.github.hooksw.konify.runtime.style

import io.github.hooksw.konify.runtime.graphics.Color
import io.github.hooksw.konify.runtime.platform.PlatformView
import io.github.hooksw.konify.runtime.unit.Density
import io.github.hooksw.konify.runtime.unit.Dp
import io.github.hooksw.konify.runtime.unit.dp
import io.github.hooksw.konify.runtime.unit.takeOrElse

internal actual class DecorationStyleNode : StyleNode<DecorationStyleAttr> {
    override val attr: DecorationStyleAttr= DecorationStyleAttr()

    override fun update(density: Density, platformView: PlatformView) {
        with(density){
            if (attr.backgroundColor == Color.Unspecified &&
                attr.borderWith == Dp.Unspecified
            ) {
                platformView.view.style.background=""
                platformView.view.style.border=""
            } else {
                platformView.view.style.backgroundColor="${attr.backgroundColor.argb}"
                platformView.view.style.border="${attr.borderWith.takeOrElse { 0.dp }.toPx()}px solid ${attr.borderColor.argb}"
            }
        }
    }
}