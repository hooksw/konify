package io.github.hooksw.konify.ui.style

import io.github.hooksw.konify.foundation.UIElementHolder
import io.github.hooksw.konify.foundation.graphics.Color
import io.github.hooksw.konify.foundation.modifier.DecorationStyleAttr
import io.github.hooksw.konify.foundation.modifier.DecorationStyleNode
import io.github.hooksw.konify.foundation.modifier.Style
import io.github.hooksw.konify.foundation.unit.Dp
import io.github.hooksw.konify.foundation.unit.dp
import io.github.hooksw.konify.foundation.unit.takeOrElse
import org.w3c.dom.HTMLElement

internal class DOMDecorationStyleNode(override val style: Style) : DecorationStyleNode<HTMLElement> {
    val finalAttr: DecorationStyleAttr = DecorationStyleAttr()

    override fun update(nativeView: UIElementHolder<HTMLElement>) {
        with(style.density) {
            if (finalAttr.backgroundColor == Color.Unspecified && finalAttr.borderWith == Dp.Unspecified) {
                nativeView.element.style. background = ""
                nativeView.element.style.border = ""
            } else {
                nativeView.element.style.backgroundColor = "${finalAttr.backgroundColor.argb}"
                nativeView.element.style.border =
                    "${finalAttr.borderWith.takeOrElse { 0.dp }.toPx()}px solid ${finalAttr.borderColor.argb}"
            }
        }
    }

}