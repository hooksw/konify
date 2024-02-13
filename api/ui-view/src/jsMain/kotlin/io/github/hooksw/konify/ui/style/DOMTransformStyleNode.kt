package io.github.hooksw.konify.ui.style

import io.github.hooksw.konify.foundation.UIElementHolder
import io.github.hooksw.konify.foundation.modifier.Style
import io.github.hooksw.konify.foundation.modifier.TransformStyleAttr
import io.github.hooksw.konify.foundation.modifier.TransformStyleNode
import org.w3c.dom.HTMLElement

class DOMTransformStyleNode(override val style: Style) : TransformStyleNode<HTMLElement> {
    val finalAttr: TransformStyleAttr = TransformStyleAttr()
    override fun update(nativeView: UIElementHolder<HTMLElement>) {
        with(style.density) {
            nativeView.element.style.transform =
                StringBuilder()
                    .append("translateX(${finalAttr.translateX.roundToPx()}px) ")
                    .append("translateY(${finalAttr.translateY.roundToPx()}px) ")
                    .append("translateZ(${finalAttr.translateZ.roundToPx()}px) ")
                    .append("scale(${finalAttr.scaleX},${finalAttr.scaleX}) ")
                    .append("rotate(${finalAttr.rotation}deg) ")
                    .append("rotateX(${finalAttr.rotationX}deg) ")
                    .append("rotateY(${finalAttr.rotationY}deg) ")
                    .toString()
        }
    }

}