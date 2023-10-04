package io.github.hooksw.konify.runtime.style

import io.github.hooksw.konify.runtime.platform.PlatformView
import io.github.hooksw.konify.runtime.unit.Density

actual class TransformStyleNode actual constructor() : StyleNode<TransformStyleAttr> {
    override val attr: TransformStyleAttr = TransformStyleAttr()

    override fun update(density: Density, platformView: PlatformView) {
        with(density) {
            platformView.view.apply {
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