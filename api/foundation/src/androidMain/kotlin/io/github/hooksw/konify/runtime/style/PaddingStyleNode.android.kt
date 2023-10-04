package io.github.hooksw.konify.runtime.style

import io.github.hooksw.konify.runtime.unit.Density
import io.github.hooksw.konify.runtime.platform.PlatformView

actual class PaddingStyleNode actual constructor() : StyleNode<PaddingAttr> {
     override val attr: PaddingAttr = PaddingAttr()

     override fun update(density: Density, platformView: PlatformView) {
        with(density) {
            platformView.view.setPadding(
                attr.paddingLeft.roundToPx(),
                attr.paddingTop.roundToPx(),
                attr.paddingRight.roundToPx(),
                attr.paddingBottom.roundToPx()
            )
        }

    }

}

