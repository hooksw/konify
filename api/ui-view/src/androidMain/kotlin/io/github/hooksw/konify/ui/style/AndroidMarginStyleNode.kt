package io.github.hooksw.konify.ui.style

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import io.github.hooksw.konify.foundation.modifier.MarginStyleNode
import io.github.hooksw.konify.foundation.modifier.Style

class AndroidMarginStyleNode(override val style: Style) : MarginStyleNode() {
    override fun onUpdate(view: Any) {
        view as View
        with(style.density) {
            (view.layoutParams as MarginLayoutParams).setMargins(
                finalAttr.marginLeft.roundToPx(),
                finalAttr.marginTop.roundToPx(),
                finalAttr.marginRight.roundToPx(),
                finalAttr.marginBottom.roundToPx()
            )
            view.requestLayout()
        }
    }
}