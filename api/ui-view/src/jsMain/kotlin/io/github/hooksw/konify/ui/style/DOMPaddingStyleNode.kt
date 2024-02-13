package io.github.hooksw.konify.ui.style

import io.github.hooksw.konify.foundation.UIElementHolder
import io.github.hooksw.konify.foundation.modifier.PaddingAttr
import io.github.hooksw.konify.foundation.modifier.PaddingElement
import io.github.hooksw.konify.foundation.modifier.Style
import org.w3c.dom.HTMLElement

class DOMPaddingStyleNode(override val style: Style) : PaddingElement<HTMLElement> {
    val finalAttr: PaddingAttr = PaddingAttr()

    override fun update(nativeView: UIElementHolder<HTMLElement>) {
        with(style.density) {
            nativeView.element.style.apply {
                paddingLeft = "${finalAttr.paddingLeft.roundToPx()}px"
                paddingTop = "${finalAttr.paddingTop.roundToPx()}px"
                paddingRight = "${finalAttr.paddingRight.roundToPx()}px"
                paddingBottom = "${finalAttr.paddingBottom.roundToPx()}px"
            }
        }
    }

}