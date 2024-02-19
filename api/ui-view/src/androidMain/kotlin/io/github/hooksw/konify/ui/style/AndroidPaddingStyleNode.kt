package io.github.hooksw.konify.ui.style

import android.view.View
import io.github.hooksw.konify.foundation.UIElementHolder
import io.github.hooksw.konify.foundation.modifier.style.PaddingAttr
import io.github.hooksw.konify.foundation.modifier.style.PaddingElement
import io.github.hooksw.konify.foundation.modifier.Style

class AndroidPaddingStyleNode(override val style: Style) : PaddingElement<View> {
    val finalAttr: PaddingAttr = PaddingAttr()

    override fun update(nativeView: UIElementHolder<View>) {
        with(style.density) {
            nativeView.element.setPadding(
                finalAttr.paddingLeft.roundToPx(),
                finalAttr.paddingTop.roundToPx(),
                finalAttr.paddingRight.roundToPx(),
                finalAttr.paddingBottom.roundToPx()
            )
        }

    }

}

