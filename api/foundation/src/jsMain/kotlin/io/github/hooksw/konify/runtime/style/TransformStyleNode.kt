package io.github.hooksw.konify.runtime.style

import io.github.hooksw.konify.runtime.unit.Density
import io.github.hooksw.konify.runtime.platform.PlatformView

actual class TransformStyleNode actual constructor() : StyleNode<TransformStyleAttr> {
    override val attr: TransformStyleAttr = TransformStyleAttr()

    override fun update(density: Density, platformView: PlatformView) {
        with(density) {
            platformView.view.style.transform =
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