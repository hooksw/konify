package io.github.hooksw.konify.ui.style

import io.github.hooksw.konify.foundation.UIElementHolder
import io.github.hooksw.konify.foundation.modifier.SizeStyleAttr
import io.github.hooksw.konify.runtime.style.SizeStyleNode
import io.github.hooksw.konify.foundation.modifier.Style
import io.github.hooksw.konify.foundation.unit.isSpecified
import org.w3c.dom.HTMLElement

class DOMSizeStyleNode(override val style: Style) : SizeStyleNode<HTMLElement> {
    override val attr: SizeStyleAttr = SizeStyleAttr()

    override fun update(nativeView: UIElementHolder<HTMLElement>) {
        with(style.density) {
            nativeView.element.style.width = "${attr.width.roundToPx()}px"
            nativeView.element.style.height = "${attr.height.roundToPx()}px"
            if (attr.maxWidth.isSpecified) {
                nativeView.element.style.maxWidth = "${attr.maxWidth.roundToPx()}px"
            }
            if (attr.maxHeight.isSpecified) {
                nativeView.element.style.maxHeight = "${attr.maxHeight.roundToPx()}px"
            }
            if (attr.minWidth.isSpecified) {
                nativeView.element.style.minWidth = "${attr.minWidth.roundToPx()}px"
            }
            if (attr.minHeight.isSpecified) {
                nativeView.element.style.minHeight = "${attr.minHeight.roundToPx()}px"
            }
        }
    }
}