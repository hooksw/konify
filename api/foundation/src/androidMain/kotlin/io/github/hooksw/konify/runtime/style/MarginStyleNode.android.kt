package io.github.hooksw.konify.runtime.style

import android.view.ViewGroup.MarginLayoutParams
import io.github.hooksw.konify.runtime.unit.Density
import io.github.hooksw.konify.runtime.platform.PlatformView

actual class MarginStyleNode : StyleNode<MarginStyleAttr> {
    override val attr: MarginStyleAttr = MarginStyleAttr()

    override fun update(density: Density, platformView: PlatformView) {
        with(density) {
            (platformView.view.layoutParams as MarginLayoutParams).setMargins(
                attr.marginLeft.roundToPx(),
                attr.marginTop.roundToPx(),
                attr.marginRight.roundToPx(),
                attr.marginBottom.roundToPx()
            )
            platformView.view.requestLayout()
        }
    }
}