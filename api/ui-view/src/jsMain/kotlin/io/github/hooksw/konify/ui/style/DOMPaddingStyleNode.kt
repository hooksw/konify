package io.github.hooksw.konify.ui.style

import io.github.hooksw.konify.foundation.UIElementHolder
import io.github.hooksw.konify.foundation.style.PaddingAttr
import io.github.hooksw.konify.foundation.style.PaddingStyleNode
import io.github.hooksw.konify.foundation.style.Style
import org.w3c.dom.HTMLElement

class DOMPaddingStyleNode(override val style: Style) : PaddingStyleNode<HTMLElement> {
    override val attr: PaddingAttr = PaddingAttr()

    override fun update(nativeView: UIElementHolder<HTMLElement>) {
        with(style.density) {
            nativeView.element.style.apply {
                paddingLeft = "${attr.paddingLeft.roundToPx()}px"
                paddingTop = "${attr.paddingTop.roundToPx()}px"
                paddingRight = "${attr.paddingRight.roundToPx()}px"
                paddingBottom = "${attr.paddingBottom.roundToPx()}px"
            }
        }
    }

}