package io.github.hooksw.konify.runtime.property

import io.github.hooksw.konify.runtime.platform.PlatformView

interface ScaleStyleProperty : Property {

    var scaleX: Float
    var scaleY: Float

}

internal  class ScaleStylePropertyImpl : ScaleStyleProperty {
    override var scaleX: Float = 1f
        set(value) {
            field = value
        }
    override var scaleY: Float = 1f
        set(value) {
            field = value
        }

    override fun applyTo(platformView: PlatformView) {

    }
}