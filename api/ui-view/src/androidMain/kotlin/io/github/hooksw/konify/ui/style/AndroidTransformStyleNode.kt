package io.github.hooksw.konify.ui.style

import android.view.View
import io.github.hooksw.konify.foundation.UIElementHolder
import io.github.hooksw.konify.foundation.style.Style
import io.github.hooksw.konify.foundation.style.TransformStyleAttr
import io.github.hooksw.konify.foundation.style.TransformStyleNode

class AndroidTransformStyleNode(override val style: Style) : TransformStyleNode<View> {
    override val attr: TransformStyleAttr = TransformStyleAttr()

    override fun update(nativeView: UIElementHolder<View>) {
        with(style.density) {
            nativeView.element.apply {
                rotation = attr.rotation
                rotationX = attr.rotationX
                rotationY = attr.rotationY

                translationX = attr.translateX.toPx()
                translationY = attr.translateY.toPx()
                translationZ = attr.translateZ.toPx()

                scaleX=attr.scaleX
                scaleY=attr.scaleY
            }
        }
    }
}