package io.github.hooksw.konify.ui.style

import io.github.hooksw.konify.foundation.UIElementHolder
import io.github.hooksw.konify.foundation.modifier.MarginStyleAttr
import io.github.hooksw.konify.foundation.modifier.MarginStyleNode
import io.github.hooksw.konify.foundation.modifier.Style
import org.w3c.dom.HTMLElement

class DOMMarginStyleNode(override val style: Style) : MarginStyleNode<HTMLElement> {
    val finalAttr: MarginStyleAttr = MarginStyleAttr()

    override fun update(nativeView: UIElementHolder<HTMLElement>) {
        with(style.density) {
            nativeView.element.style.apply {
                marginLeft = "${finalAttr.marginLeft.roundToPx()}px"
                marginTop = "${finalAttr.marginTop.roundToPx()}px"
                marginRight = "${finalAttr.marginRight.roundToPx()}px"
                marginBottom = "${finalAttr.marginBottom.roundToPx()}px"
            }
        }
    }
}