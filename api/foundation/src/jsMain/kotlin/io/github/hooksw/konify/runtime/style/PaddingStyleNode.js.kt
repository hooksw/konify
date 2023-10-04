package io.github.hooksw.konify.runtime.style

import io.github.hooksw.konify.runtime.unit.Density
import io.github.hooksw.konify.runtime.platform.PlatformView

actual class PaddingStyleNode actual constructor() : StyleNode<PaddingAttr> {
     override val attr: PaddingAttr = PaddingAttr()

     override fun update(density: Density, platformView: PlatformView) {
        with(density) {
            platformView.view.style.apply {
                paddingLeft="${attr.paddingLeft.roundToPx()}px"
                paddingTop="${attr.paddingTop.roundToPx()}px"
                paddingRight="${attr.paddingRight.roundToPx()}px"
                paddingBottom="${attr.paddingBottom.roundToPx()}px"
            }
        }
    }

}