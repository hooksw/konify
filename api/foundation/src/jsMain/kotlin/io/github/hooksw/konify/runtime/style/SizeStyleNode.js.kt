package io.github.hooksw.konify.runtime.style

import io.github.hooksw.konify.runtime.unit.Density
import io.github.hooksw.konify.runtime.unit.isSpecified
import io.github.hooksw.konify.runtime.platform.PlatformView

actual class SizeStyleNode : StyleNode<SizeStyleAttr> {
    override val attr: SizeStyleAttr = SizeStyleAttr()

    override fun update(density: Density, platformView: PlatformView) {
        with(density) {
            platformView.view.style.width = "${attr.width.roundToPx()}px"
            platformView.view.style.height = "${attr.height.roundToPx()}px"
            if (attr.maxWidth.isSpecified) {
                platformView.view.style.maxWidth = "${attr.maxWidth.roundToPx()}px"
            }
            if (attr.maxHeight.isSpecified) {
                platformView.view.style.maxHeight = "${attr.maxHeight.roundToPx()}px"
            }
            if (attr.minWidth.isSpecified) {
                platformView.view.style.minWidth = "${attr.minWidth.roundToPx()}px"
            }
            if (attr.minHeight.isSpecified) {
                platformView.view.style.minHeight = "${attr.minHeight.roundToPx()}px"
            }
        }
    }
}