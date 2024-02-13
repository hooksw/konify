package io.github.hooksw.konify.ui.style

import android.view.View
import io.github.hooksw.konify.foundation.UIElementHolder
import io.github.hooksw.konify.foundation.modifier.Style
import io.github.hooksw.konify.foundation.modifier.TransformStyleAttr
import io.github.hooksw.konify.foundation.modifier.TransformStyleNode

class AndroidTransformStyleNode(override val style: Style) : TransformStyleNode<View> {
    val finalAttr: TransformStyleAttr = TransformStyleAttr()

    override fun update(nativeView: UIElementHolder<View>) {
        with(style.density) {
            nativeView.element.apply {
                rotation = finalAttr.rotation
                rotationX = finalAttr.rotationX
                rotationY = finalAttr.rotationY

                translationX = finalAttr.translateX.toPx()
                translationY = finalAttr.translateY.toPx()
                translationZ = finalAttr.translateZ.toPx()

                scaleX=finalAttr.scaleX
                scaleY=finalAttr.scaleY
            }
        }
    }
}