package io.github.hooksw.konify.runtime.style

import io.github.hooksw.konify.runtime.unit.Density
import io.github.hooksw.konify.runtime.platform.PlatformView

actual class MarginStyleNode : StyleNode<MarginStyleAttr> {
    override val attr: MarginStyleAttr= MarginStyleAttr()

    override fun update(density: Density, platformView: PlatformView) {
        with(density){
            platformView.view.style.apply {
                marginLeft="${attr.marginLeft.roundToPx()}px"
                marginTop="${attr.marginTop.roundToPx()}px"
                marginRight="${attr.marginRight.roundToPx()}px"
                marginBottom="${attr.marginBottom.roundToPx()}px"
            }
        }
    }
}