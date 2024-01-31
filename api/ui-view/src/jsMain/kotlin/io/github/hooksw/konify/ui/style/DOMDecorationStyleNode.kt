package io.github.hooksw.konify.ui.style

import io.github.hooksw.konify.foundation.UIElementHolder
import io.github.hooksw.konify.foundation.graphics.Color
import io.github.hooksw.konify.foundation.style.DecorationStyleAttr
import io.github.hooksw.konify.foundation.style.DecorationStyleNode
import io.github.hooksw.konify.foundation.style.Style
import io.github.hooksw.konify.foundation.unit.Dp
import io.github.hooksw.konify.foundation.unit.dp
import io.github.hooksw.konify.foundation.unit.takeOrElse
import org.w3c.dom.HTMLElement

internal class DOMDecorationStyleNode(override val style: Style) : DecorationStyleNode<HTMLElement> {
    override val attr: DecorationStyleAttr = DecorationStyleAttr()

    override fun update(nativeView: UIElementHolder<HTMLElement>) {
        with(style.density) {
            if (attr.backgroundColor == Color.Unspecified && attr.borderWith == Dp.Unspecified) {
                nativeView.element.style. background = ""
                nativeView.element.style.border = ""
            } else {
                nativeView.element.style.backgroundColor = "${attr.backgroundColor.argb}"
                nativeView.element.style.border =
                    "${attr.borderWith.takeOrElse { 0.dp }.toPx()}px solid ${attr.borderColor.argb}"
            }
        }
    }

}