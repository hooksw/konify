package io.github.hooksw.konify.ui.style

import io.github.hooksw.konify.foundation.UIElementHolder
import io.github.hooksw.konify.foundation.style.MarginStyleAttr
import io.github.hooksw.konify.foundation.style.MarginStyleNode
import io.github.hooksw.konify.foundation.style.Style
import org.w3c.dom.HTMLElement

class DOMMarginStyleNode(override val style: Style) : MarginStyleNode<HTMLElement> {
    override val attr: MarginStyleAttr = MarginStyleAttr()

    override fun update(nativeView: UIElementHolder<HTMLElement>) {
        with(style.density) {
            nativeView.element.style.apply {
                marginLeft = "${attr.marginLeft.roundToPx()}px"
                marginTop = "${attr.marginTop.roundToPx()}px"
                marginRight = "${attr.marginRight.roundToPx()}px"
                marginBottom = "${attr.marginBottom.roundToPx()}px"
            }
        }
    }
}