package io.github.hooksw.konify.ui.style

import io.github.hooksw.konify.foundation.UIElementHolder
import io.github.hooksw.konify.foundation.style.Style
import io.github.hooksw.konify.foundation.style.TransformStyleAttr
import io.github.hooksw.konify.foundation.style.TransformStyleNode
import org.w3c.dom.HTMLElement

class DOMTransformStyleNode(override val style: Style) : TransformStyleNode<HTMLElement> {
    override val attr: TransformStyleAttr = TransformStyleAttr()
    override fun update(nativeView: UIElementHolder<HTMLElement>) {
        with(style.density) {
            nativeView.element.style.transform =
                StringBuilder()
                    .append("translateX(${attr.translateX.roundToPx()}px) ")
                    .append("translateY(${attr.translateY.roundToPx()}px) ")
                    .append("translateZ(${attr.translateZ.roundToPx()}px) ")
                    .append("scale(${attr.scaleX},${attr.scaleX}) ")
                    .append("rotate(${attr.rotation}deg) ")
                    .append("rotateX(${attr.rotationX}deg) ")
                    .append("rotateY(${attr.rotationY}deg) ")
                    .toString()
        }
    }

}