package io.github.hooksw.konify.ui.style

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import io.github.hooksw.konify.foundation.UIElementHolder
import io.github.hooksw.konify.foundation.style.MarginStyleAttr
import io.github.hooksw.konify.foundation.style.MarginStyleNode
import io.github.hooksw.konify.foundation.style.Style

class AndroidMarginStyleNode(override val style: Style) : MarginStyleNode {
    override val attr: MarginStyleAttr = MarginStyleAttr()

    override fun update(nativeView: UIElementHolder<*>) {
        val nativeView=nativeView as UIElementHolder<View>
        with(style.density) {
            (nativeView.element.layoutParams as MarginLayoutParams).setMargins(
                attr.marginLeft.roundToPx(),
                attr.marginTop.roundToPx(),
                attr.marginRight.roundToPx(),
                attr.marginBottom.roundToPx()
            )
            nativeView.element.requestLayout()
        }
    }
}