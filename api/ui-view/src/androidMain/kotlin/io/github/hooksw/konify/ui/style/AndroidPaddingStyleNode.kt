package io.github.hooksw.konify.ui.style

import android.view.View
import io.github.hooksw.konify.foundation.UIElementHolder
import io.github.hooksw.konify.foundation.style.PaddingAttr
import io.github.hooksw.konify.foundation.style.PaddingStyleNode
import io.github.hooksw.konify.foundation.style.Style

class AndroidPaddingStyleNode(override val style: Style) : PaddingStyleNode<View> {
    override val attr: PaddingAttr = PaddingAttr()

    override fun update(nativeView: UIElementHolder<View>) {
        with(style.density) {
            nativeView.element.setPadding(
                attr.paddingLeft.roundToPx(),
                attr.paddingTop.roundToPx(),
                attr.paddingRight.roundToPx(),
                attr.paddingBottom.roundToPx()
            )
        }

    }

}

